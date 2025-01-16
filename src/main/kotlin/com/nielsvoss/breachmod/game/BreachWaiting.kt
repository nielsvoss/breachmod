package com.nielsvoss.breachmod.game

import com.nielsvoss.breachmod.config.BreachGameConfig
import com.nielsvoss.breachmod.data.BreachMap
import com.nielsvoss.breachmod.data.RoundPersistentState
import com.nielsvoss.breachmod.ui.KitSelectorUI
import com.nielsvoss.breachmod.util.randomBottom
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.PositionFlag
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameMode
import net.minecraft.world.GameRules
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
                    private val config: BreachGameConfig, private val persistentState: RoundPersistentState,
                    private val isFirstRound: Boolean
) {
    private val availableAttackerKits = config.attackerKits.getKits()
    private val availableDefenderKits = config.defenderKits.getKits()

    companion object {
        fun open(context: GameOpenContext<BreachGameConfig>) : GameOpenProcedure {
            val config: BreachGameConfig = context.config()
            if (config.scoreNeededToWin <= 0) throw GameOpenException(Text.of("scoreNeededToWin was not positive"))
            val team1AttackingFirst: Boolean =
                if (config.teamOptions.randomizeFirstAttackingTeam) kotlin.random.Random.nextBoolean() else true
            val persistentState = RoundPersistentState(config.scoreNeededToWin, team1AttackingFirst)
            return GameOpenProcedure { gameSpace -> openInSpace(gameSpace, config, persistentState, listOf(), true) }
       }

        fun openInSpace(gameSpace: GameSpace, config: BreachGameConfig, persistentState: RoundPersistentState, playersToJoin: List<ServerPlayerEntity>, isFirstRound: Boolean) {
            val map: BreachMap = BreachMap.load(config.map.id, gameSpace.server)
            val worldConfig = RuntimeWorldConfig()
                .setGenerator(map.generator(gameSpace.server))
                .setTimeOfDay(config.map.timeOfDay)
                .setGameRule(GameRules.NATURAL_REGENERATION, !config.gameplayOptions.disableNaturalRegeneration)

            val world: ServerWorld = gameSpace.worlds.add(worldConfig)

            gameSpace.setActivity { activity ->
                val lobby: GameWaitingLobby = GameWaitingLobby.addTo(activity, WaitingLobbyConfig(
                    PlayerLimiterConfig(
                        OptionalInt.of(20), true),
                    1,
                    2,
                    WaitingLobbyConfig.Countdown.DEFAULT))
                val waiting = BreachWaiting(activity.gameSpace, world, map, config, persistentState, isFirstRound)

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
        player.health = 20.0F
        player.inventory.clear()
    }

    private fun onPlayerDeath(player: ServerPlayerEntity): EventResult {
        spawnPlayer(player)
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

        layout.addLeading { GuiElementBuilder(Items.AIR).build() }

        if (isFirstRound || config.teamOptions.allowTeamChangesAfterFirstRound) {
            layout.addLeading {
                GuiElementBuilder(Items.RED_DYE)
                    .setItemName(Text.translatable("gui.breach.join_red_team"))
                    .setCallback { _, clickType, _, _ ->
                        if (clickType.isRight) {
                            tryJoinTeam1(player)
                        }
                    }
                    .build()
            }

            layout.addLeading {
                GuiElementBuilder(Items.BLUE_DYE)
                    .setItemName(Text.translatable("gui.breach.join_blue_team"))
                    .setCallback { _, clickType, _, _ ->
                        if (clickType.isRight) {
                            tryJoinTeam2(player)
                        }
                    }
                    .build()
            }

            layout.addLeading {
                GuiElementBuilder(Items.GRAY_DYE)
                    .setItemName(Text.translatable("gui.breach.clear_team"))
                    .setCallback { _, clickType, _, _ ->
                        if (clickType.isRight) {
                            clearTeam(player)
                        }
                    }
                    .build()
            }
        } else {
            // Placeholders to take the space of team selection items in case more stuff is added to the right of them
            layout.addLeading { GuiElementBuilder(Items.AIR).build() }
            layout.addLeading { GuiElementBuilder(Items.AIR).build() }
            layout.addLeading { GuiElementBuilder(Items.AIR).build() }
        }
    }

    private fun tryJoinTeam1(player: ServerPlayerEntity) {
        if (persistentState.team1Members.contains(PlayerRef.of(player))) {
            player.sendMessage(Text.translatable("text.breach.already_in_team"))
            return
        }

        // If there are 7 or 8 players, each team can support 4 players
        val enoughSpace: Boolean = persistentState.team1Members.size < (world.players.size + 1) / 2
        if (config.teamOptions.removeTeamRestrictions || enoughSpace) {
            persistentState.team2Members.remove(PlayerRef.of(player))
            persistentState.team1Members.add(PlayerRef.of(player))
            player.sendMessage(Text.translatable("text.breach.joined_red_team"))

            persistentState.giveTeamArmor(player, config.teamOptions.giveHelmets)
        } else {
            player.sendMessage(Text.translatable("text.breach.team_full"))
        }
    }

    private fun tryJoinTeam2(player: ServerPlayerEntity) {
        if (persistentState.team2Members.contains(PlayerRef.of(player))) {
            player.sendMessage(Text.translatable("text.breach.already_in_team"))
            return
        }

        val enoughSpace: Boolean = persistentState.team2Members.size < (world.players.size + 1) / 2
        if (config.teamOptions.removeTeamRestrictions || enoughSpace) {
            persistentState.team1Members.remove(PlayerRef.of(player))
            persistentState.team2Members.add(PlayerRef.of(player))
            player.sendMessage(Text.translatable("text.breach.joined_blue_team"))

            persistentState.giveTeamArmor(player, config.teamOptions.giveHelmets)
        } else {
            player.sendMessage(Text.translatable("text.breach.team_full"))
        }
    }

    private fun clearTeam(player: ServerPlayerEntity) {
        persistentState.team1Members.remove(PlayerRef.of(player))
        persistentState.team2Members.remove(PlayerRef.of(player))
        player.sendMessage(Text.translatable("text.breach.cleared_team_selection"))

        persistentState.giveTeamArmor(player, config.teamOptions.giveHelmets)
    }

    private fun requestStart(): GameResult {
        assignUnassignedPlayers()

        BreachActive.open(gameSpace, world, map, config, persistentState)
        return GameResult.ok()
    }

    private fun assignUnassignedPlayers() {
        for (player in gameSpace.players) {
            if (PlayerRef.of(player) !in persistentState.team1Members && PlayerRef.of(player) !in persistentState.team2Members) {
                if (persistentState.team1Members.size < persistentState.team2Members.size) {
                    persistentState.team1Members.add(PlayerRef.of(player))
                } else if (persistentState.team1Members.size > persistentState.team2Members.size) {
                    persistentState.team2Members.add(PlayerRef.of(player))
                } else {
                    if (kotlin.random.Random.nextBoolean()) {
                        persistentState.team1Members.add(PlayerRef.of(player))
                    } else {
                        persistentState.team2Members.add(PlayerRef.of(player))
                    }
                }
            }
        }
    }
}
