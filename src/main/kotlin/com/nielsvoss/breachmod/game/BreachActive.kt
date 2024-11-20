package com.nielsvoss.breachmod.game

import com.nielsvoss.breachmod.*
import com.nielsvoss.breachmod.data.BreachMap
import com.nielsvoss.breachmod.data.BreachTarget
import com.nielsvoss.breachmod.state.BreachPlayersState
import com.nielsvoss.breachmod.state.BreachRoundTimer
import com.nielsvoss.breachmod.state.BreachTargetsState
import com.nielsvoss.breachmod.ui.TargetSelectorUI
import com.nielsvoss.breachmod.util.broadcast
import eu.pb4.sidebars.api.Sidebar
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import xyz.nucleoid.plasmid.game.GameOpenException
import xyz.nucleoid.plasmid.game.GameSpace
import xyz.nucleoid.plasmid.game.common.team.GameTeam
import xyz.nucleoid.plasmid.game.common.team.TeamManager
import xyz.nucleoid.plasmid.game.event.GameActivityEvents
import xyz.nucleoid.plasmid.util.PlayerRef
import kotlin.jvm.Throws

// Designed similarly to https://github.com/NucleoidMC/skywars/blob/1.20/src/main/java/us/potatoboy/skywars/game/SkyWarsActive.java
class BreachActive private constructor(private val gameSpace: GameSpace, private val world: ServerWorld,
                                       private val map: BreachMap, private val config: BreachGameConfig,
                                       private val players: BreachPlayersState) {
    companion object {
        @Throws(GameOpenException::class)
        fun open(gameSpace: GameSpace, world: ServerWorld, map: BreachMap, config: BreachGameConfig,
                 attackingTeam: GameTeam, defendingTeam: GameTeam, attackers: List<PlayerRef>, defenders: List<PlayerRef>) {
            gameSpace.setActivity { activity ->
                val breachPlayersState: BreachPlayersState = BreachPlayersState.create(activity, attackingTeam, defendingTeam, attackers, defenders)
                val breachActive = BreachActive(gameSpace, world, map, config, breachPlayersState)

                if (config.arrowsInstantKill) {
                    activity.allow(BreachRuleTypes.ARROWS_INSTANT_KILL)
                }

                activity.listen(GameActivityEvents.TICK, GameActivityEvents.Tick { breachActive.tick() })

                breachActive.start()
            }
        }
    }

    private val targetsState = BreachTargetsState(map.targets)
    private val gameSidebar = Sidebar(Sidebar.Priority.MEDIUM)
    private val roundTimer: BreachRoundTimer
    init {
        val prepTicks = config.prepLengthInSeconds * 20;
        val roundTicks = config.roundLengthInSeconds * 20;
        if (prepTicks <= 0 || roundTicks <= 0) {
            throw GameOpenException(Text.of("prepTicks and roundTicks need to be positive"))
        }
        roundTimer = BreachRoundTimer(prepTicks, roundTicks)

        buildSidebar()

        if (config.numberOfTargets < 0 || config.numberOfTargets > map.targets.size) {
            throw GameOpenException(Text.of("Invalid number of targets"))
        }
    }

    private fun buildSidebar() {
        gameSidebar.title = Text.translatable("sidebar.breach.title")
        gameSidebar.set {
            for (i in 0 until config.numberOfTargets) {
                it.add { player ->
                    val selectedTargets = targetsState.selected()
                    if (!canSeeTargets(player)) Text.of("???")
                    else if (i >= selectedTargets.size) Text.of("...")
                    else if (targetsState.isBroken(selectedTargets[i]))
                        selectedTargets[i].block.name.formatted(Formatting.STRIKETHROUGH)
                    else selectedTargets[i].block.name
                }
            }
            it.add { _ ->
                Text.of(roundTimer.displayTime())
            }
        }
    }

    private fun tick() {
        targetsState.updateBrokenTargets(world)
        if (config.outlineTargets) {
            targetsState.updateOutlines(world)
        }
        val phaseThatJustEnded: BreachRoundTimer.Phase? = roundTimer.tick()
        when (phaseThatJustEnded) {
            BreachRoundTimer.Phase.PREP_PHASE -> onEndOfPrepPhase()
            BreachRoundTimer.Phase.MAIN_PHASE -> {}
            null -> {}
        }
    }

    private fun onEndOfPrepPhase() {
        if (targetsState.selected().size < config.numberOfTargets) {
            targetsState.populate(config.numberOfTargets)
            players.survivingDefenders().broadcast(Text.translatable("text.breach.randomly_selected_targets"))
        }
    }

    private fun start() {
        gameSidebar.show()
        // TODO: Handle offline players
        for (player in players.onlineParticipants()) {
            gameSidebar.addPlayer(player)
        }

        for (player in players.survivingDefenders()) {
            TargetSelectorUI.open(player, map.targets) { target ->
                trySelectTarget(player, target)
            }
        }
    }

    private fun canSeeTargets(player: ServerPlayerEntity): Boolean {
        // TODO: Make it so that in prep time, only defenders can see selected targets
        return true;
    }

    private fun trySelectTarget(player: ServerPlayerEntity, target: BreachTarget) {
        if (targetsState.selected().size >= config.numberOfTargets) {
            player.sendMessage(Text.translatable("text.breach.all_targets_selected"))
            return
        }

        if (target in targetsState.selected()) {
            player.sendMessage(Text.translatable("text.breach.target_already_selected"))
            return
        }

        targetsState.selectTarget(target)
        player.sendMessage(Text.translatable("text.breach.target_selected"))
    }
}