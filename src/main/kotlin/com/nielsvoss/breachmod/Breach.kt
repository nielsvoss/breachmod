package com.nielsvoss.breachmod

import com.nielsvoss.breachmod.game.BreachWaiting
import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory
import xyz.nucleoid.plasmid.game.GameType


object Breach : ModInitializer {
	const val MOD_ID: String = "breach"

    private val logger = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		GameType.register(Identifier.of(MOD_ID, "breach"), BreachGameConfig.CODEC, BreachWaiting::open)
	}
}