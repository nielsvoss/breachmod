package com.nielsvoss.breachmod

import eu.pb4.sidebars.api.Sidebar
import eu.pb4.sidebars.api.lines.SuppliedSidebarLine
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.entity.EntityType
import net.minecraft.text.Text
import org.slf4j.LoggerFactory

object Breach : ModInitializer {
    private val logger = LoggerFactory.getLogger("breach")

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		logger.info("Hello Fabric world!")
		BreachConfig.saveDefaults()

		ServerLivingEntityEvents.AFTER_DAMAGE.register { entity, source, _, _, blocked ->
			if (!blocked && source.source?.type == EntityType.ARROW) {
				val instantKillArrows = BreachConfig.instantKillArrows.get()
				if (instantKillArrows) {
					entity.damage(source, Float.MAX_VALUE)
				}
			}
		}

		val round = Round.startNew(listOf())
		ServerTickEvents.END_SERVER_TICK.register { server ->
			server.playerManager.playerList.forEach { round.addParticipant(it) }
			round.tick()
		}
	}
}