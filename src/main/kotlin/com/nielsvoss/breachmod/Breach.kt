package com.nielsvoss.breachmod

import com.nielsvoss.breachmod.game.BreachWaiting
import com.nielsvoss.breachmod.item.ExplosiveArrowItem
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory
import xyz.nucleoid.plasmid.game.GameType


object Breach : ModInitializer {
	const val MOD_ID: String = "breach"

    private val logger = LoggerFactory.getLogger(MOD_ID)

	@JvmField
	val EXPLOSIVE_ARROW: Item = ExplosiveArrowItem(FabricItemSettings())

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "explosive_arrow"), EXPLOSIVE_ARROW)

		GameType.register(Identifier.of(MOD_ID, "breach"), BreachGameConfig.CODEC, BreachWaiting::open)
	}
}