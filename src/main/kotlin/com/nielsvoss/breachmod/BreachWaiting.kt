package com.nielsvoss.breachmod

import net.minecraft.block.Blocks
import net.minecraft.scoreboard.AbstractTeam
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameMode
import xyz.nucleoid.fantasy.RuntimeWorldConfig
import xyz.nucleoid.map_templates.MapTemplate
import xyz.nucleoid.plasmid.game.GameOpenContext
import xyz.nucleoid.plasmid.game.GameOpenProcedure
import xyz.nucleoid.plasmid.game.GameResult
import xyz.nucleoid.plasmid.game.GameSpace
import xyz.nucleoid.plasmid.game.common.team.GameTeam
import xyz.nucleoid.plasmid.game.common.team.GameTeamConfig
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey
import xyz.nucleoid.plasmid.game.common.team.TeamAllocator
import xyz.nucleoid.plasmid.game.event.GameActivityEvents
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents
import xyz.nucleoid.plasmid.game.player.PlayerOffer
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult
import xyz.nucleoid.plasmid.game.rule.GameRuleType
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator

// Design inspired by https://github.com/NucleoidMC/skywars/blob/1.20/src/main/java/us/potatoboy/skywars/game/SkyWarsWaiting.java
class BreachWaiting(private val gameSpace: GameSpace, private val world: ServerWorld, private val config: BreachGameConfig) {
    private val team1 = createTeam("Breach1", DyeColor.RED)
    private val team2 = createTeam("Breach2", DyeColor.BLUE)
    private val teams = listOf(team1, team2)

    companion object {
        fun open(context: GameOpenContext<BreachGameConfig>) : GameOpenProcedure {
            val config: BreachGameConfig = context.config()

            val template = MapTemplate.createEmpty()
            template.setBlockState(BlockPos(0, 64, 0), Blocks.STONE.defaultState)

            val generator: TemplateChunkGenerator = TemplateChunkGenerator(context.server, template)
            val worldConfig = RuntimeWorldConfig()
                .setGenerator(generator)
                .setTimeOfDay(6000)


            return context.openWithWorld(worldConfig) { activity, world ->
                val waiting = BreachWaiting(activity.gameSpace, world, config)

                activity.deny(GameRuleType.HUNGER)

                if (config.arrowsInstantKill) {
                    activity.allow(BreachRuleTypes.ARROWS_INSTANT_KILL)
                }

                activity.listen(GamePlayerEvents.OFFER, GamePlayerEvents.Offer { waiting.offer(it) })
                activity.listen(GameActivityEvents.REQUEST_START, GameActivityEvents.RequestStart { waiting.requestStart() })
            }
        }
    }

    private fun offer(offer: PlayerOffer): PlayerOfferResult {
        val player: ServerPlayerEntity = offer.player
        return offer.accept(world, Vec3d(0.0, 65.0, 0.0)).and {
            player.changeGameMode(GameMode.ADVENTURE)
        }
    }

    private fun requestStart(): GameResult {
        val teamAllocator = TeamAllocator<GameTeam, ServerPlayerEntity>(teams)
        for (player in gameSpace.players) {
            teamAllocator.add(player, null)
        }
        teamAllocator.allocate { team, player ->
            player.sendMessage(Text.literal(team.key.toString()))
        }

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
