package com.nielsvoss.breachmod

import net.minecraft.block.Blocks
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameMode
import xyz.nucleoid.fantasy.RuntimeWorldConfig
import xyz.nucleoid.map_templates.MapTemplate
import xyz.nucleoid.plasmid.game.GameOpenContext
import xyz.nucleoid.plasmid.game.GameOpenProcedure
import xyz.nucleoid.plasmid.game.GameSpace
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents
import xyz.nucleoid.plasmid.game.player.PlayerOffer
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult
import xyz.nucleoid.plasmid.game.rule.GameRuleType
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator

class BreachWaiting(private val gameSpace: GameSpace, private val world: ServerWorld, private val config: BreachGameConfig) {
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
            }
        }
    }

    fun offer(offer: PlayerOffer): PlayerOfferResult {
        val player: ServerPlayerEntity = offer.player
        return offer.accept(world, Vec3d(0.0, 65.0, 0.0)).and {
            player.changeGameMode(GameMode.ADVENTURE)
        }
    }
}
