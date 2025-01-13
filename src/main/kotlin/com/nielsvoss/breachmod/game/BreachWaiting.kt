package com.nielsvoss.breachmod.game

import com.nielsvoss.breachmod.BreachGameConfig
import com.nielsvoss.breachmod.data.BreachMap
import com.nielsvoss.breachmod.ui.KitSelectorUI
import com.nielsvoss.breachmod.util.randomBottom
import eu.pb4.sgui.api.elements.GuiElementBuilder
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.PositionFlag
import net.minecraft.scoreboard.AbstractTeam
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.DyeColor
import net.minecraft.util.Hand
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameMode
import xyz.nucleoid.fantasy.RuntimeWorldConfig
import xyz.nucleoid.plasmid.api.game.GameOpenContext
import xyz.nucleoid.plasmid.api.game.GameOpenProcedure
import xyz.nucleoid.plasmid.api.game.GameSpace
import xyz.nucleoid.plasmid.api.game.common.GameWaitingLobby
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType
import xyz.nucleoid.plasmid.api.game.GameResult
import xyz.nucleoid.plasmid.api.game.common.config.PlayerLimiterConfig
import xyz.nucleoid.plasmid.api.game.common.config.WaitingLobbyConfig
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamConfig
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey
import xyz.nucleoid.plasmid.api.game.common.team.TeamAllocator
import xyz.nucleoid.plasmid.api.game.common.ui.WaitingLobbyUiLayout
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents
import xyz.nucleoid.plasmid.api.game.event.GamePlayerEvents
import xyz.nucleoid.plasmid.api.game.event.GameWaitingLobbyEvents
import xyz.nucleoid.plasmid.api.util.PlayerRef
import xyz.nucleoid.stimuli.event.EventResult
import xyz.nucleoid.stimuli.event.item.ItemUseEvent
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent
import java.util.*

// Design inspired by https://github.com/NucleoidMC/skywars/blob/1.20/src/main/java/us/potatoboy/skywars/game/SkyWarsWaiting.java
class BreachWaiting(private val gameSpace: GameSpace, private val world: ServerWorld, private val map: BreachMap,
                    private val config: BreachGameConfig
) {
    private val team1 = createTeam("Breach1", Text.translatable("team.breach.red"), DyeColor.RED)
    private val team2 = createTeam("Breach2", Text.translatable("team.breach.blue"), DyeColor.BLUE)

    companion object {
        fun open(context: GameOpenContext<BreachGameConfig>) : GameOpenProcedure {
            val config: BreachGameConfig = context.config()
            val map: BreachMap = BreachMap.load(config.map, context.server)

            val worldConfig = RuntimeWorldConfig()
                .setGenerator(map.generator(context.server))
                .setTimeOfDay(config.timeOfDay)

            return context.openWithWorld(worldConfig) { activity, world ->
                GameWaitingLobby.addTo(activity, WaitingLobbyConfig(
                    PlayerLimiterConfig(
                        OptionalInt.of(20), true),
                        1,
                        2,
                        WaitingLobbyConfig.Countdown.DEFAULT))
                val waiting = BreachWaiting(activity.gameSpace, world, map, config)

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

                activity.listen(GamePlayerEvents.ACCEPT, GamePlayerEvents.Accept { offer ->
                    offer.teleport(world, map.lobbySpawnRegion.bounds.randomBottom())
                        .thenRunForEach { player -> player.changeGameMode(GameMode.ADVENTURE) }
                })
                activity.listen(PlayerDeathEvent.EVENT, PlayerDeathEvent { player, _ -> waiting.onPlayerDeath(player) })
                activity.listen(GameActivityEvents.REQUEST_START, GameActivityEvents.RequestStart { waiting.requestStart() })

                activity.listen(GameWaitingLobbyEvents.BUILD_UI_LAYOUT,
                    GameWaitingLobbyEvents.BuildUiLayout { layout, player -> waiting.onBuildUiLayout(layout, player) })

                // activity.listen(ItemUseEvent.EVENT, ItemUseEvent { player, hand -> waiting.onUseItem(player, hand) })
            }
        }
    }

    private fun onPlayerDeath(player: ServerPlayerEntity): EventResult {
        player.health = 20.0F
        val respawnLocation: Vec3d = map.lobbySpawnRegion.bounds.randomBottom()
        player.teleport(world, respawnLocation.x, respawnLocation.y, respawnLocation.z, PositionFlag.VALUES, 0.0F, 0.0F, true)
        return EventResult.DENY
    }

    /*
    private fun onUseItem(player: ServerPlayerEntity, hand: Hand): ActionResult {
        if (player.inventory.mainHandStack.item == Items.BOW) {
            println("hi")
            KitSelectorUI.test(player)
        }

        return ActionResult.PASS
    }
     */

    private fun onBuildUiLayout(layout: WaitingLobbyUiLayout, player: ServerPlayerEntity) {
        layout.addLeading {
            GuiElementBuilder(Items.BOW)
                .setCallback { index, type, action, gui ->
                    KitSelectorUI.test(player)
                }
                .build()
        }
    }

    private fun requestStart(): GameResult {
        val teamAllocator = TeamAllocator<GameTeam, ServerPlayerEntity>(listOf(team1, team2))
        for (player in gameSpace.players) {
            teamAllocator.add(player, null)
        }

        // TODO: Make it so that team1 isn't always attacking
        val attackers = mutableListOf<PlayerRef>()
        val defenders = mutableListOf<PlayerRef>()
        teamAllocator.allocate { team, player ->
            when (team.key) {
                team1.key -> attackers.add(PlayerRef.of(player))
                team2.key -> defenders.add(PlayerRef.of(player))
                else -> throw AssertionError("Player was not allocated to a team")
            }
        }

        BreachActive.open(gameSpace, world, map, config, team1, team2, attackers, defenders, team1.key)
        return GameResult.ok()
    }

    private fun createTeam(id: String, name: Text, color: DyeColor): GameTeam {
        return GameTeam(GameTeamKey(id),
            GameTeamConfig.builder()
                .setCollision(AbstractTeam.CollisionRule.NEVER)
                .setColors(GameTeamConfig.Colors.from(color))
                .setName(name)
                .build())
    }
}
