package com.nielsvoss.breachmod

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.block.Blocks
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameMode
import org.slf4j.LoggerFactory
import xyz.nucleoid.fantasy.RuntimeWorldConfig
import xyz.nucleoid.map_templates.MapTemplate
import xyz.nucleoid.plasmid.game.GameType
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents
import xyz.nucleoid.plasmid.game.rule.GameRuleType
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator


object Breach : ModInitializer {
	const val MOD_ID: String = "breach"

    private val logger = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		logger.info("Hello Fabric world!")
		BreachConfig.saveDefaults()

		/*ServerLivingEntityEvents.AFTER_DAMAGE.register { entity, source, _, _, blocked ->
			if (!blocked && source.source?.type == EntityType.ARROW) {
				val instantKillArrows = BreachConfig.instantKillArrows.get()
				if (instantKillArrows) {
					entity.damage(source, Float.MAX_VALUE)
				}
			}
		}*/

		val round = Round.startNew(listOf())
		ServerTickEvents.END_SERVER_TICK.register { server ->
			server.playerManager.playerList.forEach { round.addParticipant(it) }
			round.tick()
		}

		GameType.register(Identifier.of(MOD_ID, "breach"), BreachGameConfig.CODEC, BreachWaiting::open)
	}
}