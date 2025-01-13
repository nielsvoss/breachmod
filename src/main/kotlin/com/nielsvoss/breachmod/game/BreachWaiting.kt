package com.nielsvoss.breachmod.game

import com.nielsvoss.breachmod.BreachGameConfig
import com.nielsvoss.breachmod.data.BreachMap
import com.nielsvoss.breachmod.data.RoundPersistentState
import com.nielsvoss.breachmod.ui.KitSelectorUI
import com.nielsvoss.breachmod.util.randomBottom
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.PositionFlag
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameMode
import xyz.nucleoid.fantasy.RuntimeWorldConfig
import xyz.nucleoid.plasmid.api.game.GameOpenContext
import xyz.nucleoid.plasmid.api.game.GameOpenException
import xyz.nucleoid.plasmid.api.game.GameOpenProcedure
import xyz.nucleoid.plasmid.api.game.GameSpace
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType
import xyz.nucleoid.plasmid.api.game.GameResult
import xyz.nucleoid.plasmid.api.game.common.config.PlayerLimiterConfig
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam
import xyz.nucleoid.plasmid.api.game.common.team.TeamAllocator
import xyz.nucleoid.plasmid.api.game.common.ui.WaitingLobbyUiLayout
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents
import xyz.nucleoid.plasmid.api.game.event.GameWaitingLobbyEvents
import xyz.nucleoid.plasmid.api.util.PlayerRef
import xyz.nucleoid.stimuli.event.EventResult
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent
import java.util.*

// Design inspired by https://github.com/NucleoidMC/skywars/blob/1.20/src/main/java/us/potatoboy/skywars/game/SkyWarsWaiting.java
class BreachWaiting(private val gameSpace: GameSpace, private val world: ServerWorld, private val map: BreachMap,
                    private val config: BreachGameConfig, private val persistentState: RoundPersistentState
) {
    private val availableAttackerKits = config.attackerKits.getKits()
    private val availableDefenderKits = config.defenderKits.getKits()

    companion object {
        fun open(context: GameOpenContext<BreachGameConfig>) : GameOpenProcedure {
            val config: BreachGameConfig = context.config()
            if (config.scoreNeededToWin <= 0) throw GameOpenException(Text.of("scoreNeededToWin was not positive"))
            val persistentState = RoundPersistentState(config.scoreNeededToWin)
            return GameOpenProcedure { gameSpace -> openInSpace(gameSpace, config, persistentState, listOf()) }
       }

        fun openInSpace(gameSpace: GameSpace, config: BreachGameConfig, persistentState: RoundPersistentState, playersToJoin: List<ServerPlayerEntity>) {
            val map: BreachMap = BreachMap.load(config.map, gameSpace.server)
            val worldConfig = RuntimeWorldConfig()
                .setGenerator(map.generator(gameSpace.server))
                .setTimeOfDay(config.timeOfDay)

            val world: ServerWorld = gameSpace.worlds.add(worldConfig)

            gameSpace.setActivity { activity ->
                val lobby: GameWaitingLobby = GameWaitingLobby.addTo(activity, WaitingLobbyConfig(
                    PlayerLimiterConfig(
                        OptionalInt.of(20), true),
                    1,
                    2,
                    WaitingLobbyConfig.Countdown.DEFAULT))
                val waiting = BreachWaiting(activity.gameSpace, world, map, config, persistentState)

                activity.deny(GameRuleType.HUNGER)
                activity.deny(GameRuleType.PVP)
                activity.deny(GameRuleType.CRAFTING)
                activity.deny(GameRuleType.THROW_ITEMS)

                activity.deny(GameRuleType.PORTALS)
                activity.deny(GameRuleType.FIRE_TICK)
                activity.deny(GameRuleType.CORAL_DEATH)
                activity.deny(GameRuleType.ICE_MELT)
                activity.deny(GameRuleType.FLUID_FLOW)
                activity.deny(GameRuleType.DISPENSER_ACTIVATE)

                activity.listen(PlayerDeathEvent.EVENT, PlayerDeathEvent { player, _ -> waiting.onPlayerDeath(player) })
                activity.listen(GameActivityEvents.REQUEST_START, GameActivityEvents.RequestStart { waiting.requestStart() })

                activity.listen(GameWaitingLobbyEvents.BUILD_UI_LAYOUT,
                    GameWaitingLobbyEvents.BuildUiLayout { layout, player -> waiting.onBuildUiLayout(layout, player) })

                activity.listen(GamePlayerEvents.ACCEPT, GamePlayerEvents.Accept { offer ->
                    offer.teleport(world, waiting.spawnLocation())
                        .thenRunForEach { player -> waiting.spawnPlayer(player) }
                })

                val spawnLocation = waiting.spawnLocation()
                for (player in playersToJoin) {
                    player.teleport(world, spawnLocation.x, spawnLocation.y, spawnLocation.z, setOf(), 0F, 0F, true)
                }
            }
        }
    }

    private fun spawnLocation(): Vec3d {
        return map.lobbySpawnRegion.bounds.randomBottom()
    }

    private fun spawnPlayer(player: ServerPlayerEntity) {
        player.changeGameMode(GameMode.ADVENTURE)
    }

    private fun onPlayerDeath(player: ServerPlayerEntity): EventResult {
        player.health = 20.0F
        val respawnLocation: Vec3d = map.lobbySpawnRegion.bounds.randomBottom()
        player.teleport(world, respawnLocation.x, respawnLocation.y, respawnLocation.z, PositionFlag.VALUES, 0.0F, 0.0F, true)
        return EventResult.DENY
    }

    private fun onBuildUiLayout(layout: WaitingLobbyUiLayout, player: ServerPlayerEntity) {
        layout.addLeading {
            GuiElementBuilder(Items.BOW)
                .setItemName(Text.translatable("gui.breach.attacker_kit_selection_item"))
                .setCallback { _, _, _, _ ->
                    KitSelectorUI.open(player, availableAttackerKits, Text.translatable("gui.breach.select_attacker_kit")) { _, kit ->
                        player.sendMessage(Text.translatable("text.breach.selected_attacker_kit").append(kit.getName()))
                        persistentState.kitSelections.setAttackerKit(player, kit)
                    }
                }
                .build()
        }

        layout.addLeading {
            GuiElementBuilder(Items.IRON_DOOR)
                .setItemName(Text.translatable("gui.breach.defender_kit_selection_item"))
                .setCallback { _, _, _, _ ->
                    KitSelectorUI.open(player, availableDefenderKits, Text.translatable("gui.breach.select_defender_kit")) { _, kit ->
                        player.sendMessage(Text.translatable("text.breach.selected_defender_kit").append(kit.getName()))
                        persistentState.kitSelections.setDefenderKit(player, kit)
                    }
                }
                .build()
        }
    }

    private fun requestStart(): GameResult {
        val teamAllocator = TeamAllocator<GameTeam, ServerPlayerEntity>(listOf(persistentState.team1, persistentState.team2))
        for (player in gameSpace.players) {
            teamAllocator.add(player, null)
        }

        // TODO: Make it so that team1 isn't always attacking
        val attackers = mutableListOf<PlayerRef>()
        val defenders = mutableListOf<PlayerRef>()
        teamAllocator.allocate { team, player ->
            when (team.key) {
                persistentState.team1.key -> attackers.add(PlayerRef.of(player))
                persistentState.team2.key -> defenders.add(PlayerRef.of(player))
                else -> throw AssertionError("Player was not allocated to a team")
            }
        }
        persistentState.getAttackingTeamMembers().clear()
        persistentState.getAttackingTeamMembers().addAll(attackers)
        persistentState.getDefendingTeamMembers().clear()
        persistentState.getDefendingTeamMembers().addAll(defenders)

        BreachActive.open(gameSpace, world, map, config, persistentState)
        return GameResult.ok()
    }
}
