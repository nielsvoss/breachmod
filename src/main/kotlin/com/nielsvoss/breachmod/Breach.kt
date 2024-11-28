package com.nielsvoss.breachmod

import com.nielsvoss.breachmod.entity.ExplosiveArrowEntity
import com.nielsvoss.breachmod.game.BreachWaiting
import com.nielsvoss.breachmod.item.ExplosiveArrowItem
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityType
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
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
	/*
	@JvmField
	val EXPLOSIVE_ARROW_ENTITY: EntityType<ExplosiveArrowEntity> = Registry.register(
		Registries.ENTITY_TYPE,
		Identifier.of(MOD_ID, "explosive_arrow"),
		EntityType.Builder.create<ExplosiveArrowEntity>(::ExplosiveArrowEntity, SpawnGroup.MISC)
			.setDimensions(0.5F, 0.5F).maxTrackingRange(4).trackingTickInterval(20).build("explosive_arrow"))
	 */
	val EXPLOSIVE_ARROW_ENTITY: EntityType<ExplosiveArrowEntity> = EntityType.Builder.create(::ExplosiveArrowEntity, SpawnGroup.MISC)
		.setDimensions(0.5F, 0.5F).maxTrackingRange(4).trackingTickInterval(20).build()

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "explosive_arrow"), EXPLOSIVE_ARROW)
		Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, "explosive_arrow"), EXPLOSIVE_ARROW_ENTITY)
		PolymerEntityUtils.registerType(EXPLOSIVE_ARROW_ENTITY)

		GameType.register(Identifier.of(MOD_ID, "breach"), BreachGameConfig.CODEC, BreachWaiting::open)
	}
}