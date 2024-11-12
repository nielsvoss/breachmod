package com.nielsvoss.breachmod

import eu.pb4.sidebars.api.Sidebar
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import xyz.nucleoid.plasmid.game.GameOpenException
import xyz.nucleoid.plasmid.game.GameSpace
import xyz.nucleoid.plasmid.game.common.team.GameTeam
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey
import xyz.nucleoid.plasmid.game.common.team.TeamManager
import xyz.nucleoid.plasmid.game.event.GameActivityEvents
import xyz.nucleoid.plasmid.util.PlayerRef
import kotlin.jvm.Throws

// Designed similarly to https://github.com/NucleoidMC/skywars/blob/1.20/src/main/java/us/potatoboy/skywars/game/SkyWarsActive.java
class BreachActive private constructor(private val gameSpace: GameSpace, private val world: ServerWorld,
                                       private val map: BreachMap, private val config: BreachGameConfig,
                                       private val attackingTeam: GameTeam, private val defendingTeam: GameTeam,
                                       private val teamManager: TeamManager) {
    companion object {
        @Throws(GameOpenException::class)
        fun open(gameSpace: GameSpace, world: ServerWorld, map: BreachMap, config: BreachGameConfig,
                 attackingTeam: GameTeam, defendingTeam: GameTeam, attackers: List<PlayerRef>, defenders: List<PlayerRef>) {
            gameSpace.setActivity { activity ->
                val teamManager: TeamManager = TeamManager.addTo(activity)
                teamManager.addTeam(attackingTeam)
                teamManager.addTeam(defendingTeam)
                for (player in attackers) {
                    teamManager.addPlayerTo(player, attackingTeam.key)
                }
                for (player in defenders) {
                    teamManager.addPlayerTo(player, defendingTeam.key)
                }

                val breachActive = BreachActive(gameSpace, world, map, config, attackingTeam, defendingTeam, teamManager)

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
        var prepTicks = config.prepLengthInSeconds * 20;
        var roundTicks = config.roundLengthInSeconds * 20;
        if (prepTicks <= 0) {
            broadcastServerError("Configuration Error: prepLengthInSeconds was a nonpositve number")
            prepTicks = 1
        }
        if (roundTicks <= 0) {
            broadcastServerError("Configuration Error: roundLengthInSeconds was a nonpositive number")
            roundTicks = 1
        }
        roundTimer = BreachRoundTimer(prepTicks, roundTicks)

        buildSidebar()
        targetsState.selectTarget(map.targets[0])

        if (config.numberOfTargets < 0 || config.numberOfTargets > map.targets.size) {
            throw GameOpenException(Text.literal("Invalid number of targets"))
        }
    }

    private fun buildSidebar() {
        gameSidebar.title = Text.translatable("breach.sidebar.title")
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
                Text.literal(roundTimer.displayTime())
            }
        }
    }

    private fun tick() {
        targetsState.updateBrokenTargets(world)
        val phaseThatJustEnded: BreachRoundTimer.Phase? = roundTimer.tick()
        if (phaseThatJustEnded != null) {
            broadcast(Text.literal("Phase just ended"))
        }
    }

    private fun start() {
        gameSidebar.show()
        // TODO: Handle offline players
        for (player in onlineParticipants()) {
            gameSidebar.addPlayer(player)
        }

        for (player in survivingDefenders()) {
            TargetSelectorUI.open(player, map.targets) { target ->
                trySelectTarget(player, target)
            }
        }
    }

    private fun survivingAttackers(): List<ServerPlayerEntity> {
        return survivingParticipants().filter {
            it in teamManager.playersIn(attackingTeam.key)
        }
    }

    private fun survivingDefenders(): List<ServerPlayerEntity> {
        return survivingParticipants().filter {
            it in teamManager.playersIn(defendingTeam.key)
        }
    }

    private fun survivingParticipants(): List<ServerPlayerEntity> {
        return onlineParticipants()
    }

    private fun onlineParticipants(): List<ServerPlayerEntity> {
        val players = mutableListOf<ServerPlayerEntity>()
        players.addAll(teamManager.playersIn(attackingTeam.key))
        players.addAll(teamManager.playersIn(defendingTeam.key))
        return players
    }

    private fun allParticipants(): List<PlayerRef> {
        val players = mutableListOf<PlayerRef>()
        players.addAll(teamManager.allPlayersIn(attackingTeam.key))
        players.addAll(teamManager.allPlayersIn(defendingTeam.key))
        return players
    }

    private fun broadcast(text: Text) {
        onlineParticipants().forEach { it.sendMessage(text) }
    }

    private fun broadcastServerError(message: String) {
        broadcast(Text.literal(message))
        broadcast(Text.literal("Please contact the server admins."))
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