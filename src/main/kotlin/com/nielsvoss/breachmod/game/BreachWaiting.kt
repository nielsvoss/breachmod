package com.nielsvoss.breachmod.game

import com.nielsvoss.breachmod.BreachGameConfig
import com.nielsvoss.breachmod.data.BreachMap
import com.nielsvoss.breachmod.util.randomBottom
import net.minecraft.scoreboard.AbstractTeam
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.DyeColor
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameMode
import xyz.nucleoid.fantasy.RuntimeWorldConfig
import xyz.nucleoid.plasmid.game.GameOpenContext
import xyz.nucleoid.plasmid.game.GameOpenProcedure
import xyz.nucleoid.plasmid.game.GameResult
import xyz.nucleoid.plasmid.game.GameSpace
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig
import xyz.nucleoid.plasmid.game.common.team.GameTeam
import xyz.nucleoid.plasmid.game.common.team.GameTeamConfig
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey
import xyz.nucleoid.plasmid.game.common.team.TeamAllocator
import xyz.nucleoid.plasmid.game.event.GameActivityEvents
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents
import xyz.nucleoid.plasmid.game.player.PlayerOffer
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult
import xyz.nucleoid.plasmid.game.rule.GameRuleType
import xyz.nucleoid.plasmid.util.PlayerRef
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent

// Design inspired by https://github.com/NucleoidMC/skywars/blob/1.20/src/main/java/us/potatoboy/skywars/game/SkyWarsWaiting.java
class BreachWaiting(private val gameSpace: GameSpace, private val world: ServerWorld, private val map: BreachMap,
                    private val config: BreachGameConfig
) {
    private val team1 = createTeam("Breach1", DyeColor.RED)
    private val team2 = createTeam("Breach2", DyeColor.BLUE)

    companion object {
        fun open(context: GameOpenContext<BreachGameConfig>) : GameOpenProcedure {
            val config: BreachGameConfig = context.config()
            val map: BreachMap = BreachMap.load(config.map, context.server)

            val worldConfig = RuntimeWorldConfig()
                .setGenerator(map.generator(context.server))
                .setTimeOfDay(config.timeOfDay)

            return context.openWithWorld(worldConfig) { activity, world ->
                GameWaitingLobby.addTo(activity, PlayerConfig(1, 10, 2, PlayerConfig.Countdown.DEFAULT))
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

                activity.listen(GamePlayerEvents.OFFER, GamePlayerEvents.Offer { waiting.offer(it) })
                activity.listen(PlayerDeathEvent.EVENT, PlayerDeathEvent { player, _ -> waiting.onPlayerDeath(player) })
                activity.listen(GameActivityEvents.REQUEST_START, GameActivityEvents.RequestStart { waiting.requestStart() })
            }
        }
    }

    private fun offer(offer: PlayerOffer): PlayerOfferResult {
        val player: ServerPlayerEntity = offer.player
        val spawnLocation: Vec3d = map.lobbySpawnRegion.bounds.randomBottom()
        return offer.accept(world, spawnLocation).and {
            player.changeGameMode(GameMode.ADVENTURE)
        }
    }

    private fun onPlayerDeath(player: ServerPlayerEntity): ActionResult {
        player.health = 20.0F
        val respawnLocation: Vec3d = map.lobbySpawnRegion.bounds.randomBottom()
        player.teleport(world, respawnLocation.x, respawnLocation.y, respawnLocation.z, 0.0F, 0.0F)
        return ActionResult.FAIL
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

        BreachActive.open(gameSpace, world, map, config, team1, team2, attackers, defenders)
        return GameResult.ok()
    }

    private fun createTeam(id: String, color: DyeColor): GameTeam {
        return GameTeam(GameTeamKey(id),
            GameTeamConfig.builder()
                .setCollision(AbstractTeam.CollisionRule.NEVER)
                .setColors(GameTeamConfig.Colors.from(color))
                .build())
    }
}
