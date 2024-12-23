package com.nielsvoss.breachmod

import com.nielsvoss.breachmod.entity.EndermiteMorphEntity
import com.nielsvoss.breachmod.entity.GrappleEntity
import com.nielsvoss.breachmod.entity.SilverfishMorphEntity
import com.nielsvoss.breachmod.game.BreachWaiting
import com.nielsvoss.breachmod.item.EnderArrowItem
import com.nielsvoss.breachmod.item.ExplosiveArrowItem
import com.nielsvoss.breachmod.item.GrapplingArrowItem
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.minecraft.block.DispenserBlock
import net.minecraft.block.dispenser.ProjectileDispenserBehavior
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import net.minecraft.util.math.Position
import net.minecraft.world.World
import org.slf4j.LoggerFactory
import xyz.nucleoid.plasmid.game.GameType


object Breach : ModInitializer {
	const val MOD_ID: String = "breach"

    private val logger = LoggerFactory.getLogger(MOD_ID)

	@JvmField
	val EXPLOSIVE_ARROW: Item = ExplosiveArrowItem(FabricItemSettings())
	@JvmField
	val ENDER_ARROW: Item = EnderArrowItem(FabricItemSettings())
	@JvmField
	val GRAPPLING_ARROW: Item = GrapplingArrowItem(FabricItemSettings())

	@JvmField
	val GRAPPLE_ENTITY_TYPE: EntityType<GrappleEntity> =
		EntityType.Builder.create(::GrappleEntity, SpawnGroup.CREATURE).build("grapple")
	@JvmField
	val SILVERFISH_MORPH_ENTITY_TYPE: EntityType<SilverfishMorphEntity> =
		EntityType.Builder.create(::SilverfishMorphEntity, SpawnGroup.MISC).build("silverfish_morph")
	@JvmField
	val ENDERMITE_MORPH_ENTITY_TYPE: EntityType<EndermiteMorphEntity> =
		EntityType.Builder.create(::EndermiteMorphEntity, SpawnGroup.MISC).build("endermite_morph")

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "explosive_arrow"), EXPLOSIVE_ARROW)
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "ender_arrow"), ENDER_ARROW)
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "grappling_arrow"), GRAPPLING_ARROW)

		Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, "grapple"), GRAPPLE_ENTITY_TYPE)
		Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, "silverfish_morph"), SILVERFISH_MORPH_ENTITY_TYPE)
		Registry.register(Registries.ENTITY_TYPE, Identifier.of(MOD_ID, "endermite_morph"), ENDERMITE_MORPH_ENTITY_TYPE)
		FabricDefaultAttributeRegistry.register(GRAPPLE_ENTITY_TYPE, MobEntity.createMobAttributes());
		FabricDefaultAttributeRegistry.register(SILVERFISH_MORPH_ENTITY_TYPE, MobEntity.createMobAttributes());
		FabricDefaultAttributeRegistry.register(ENDERMITE_MORPH_ENTITY_TYPE, MobEntity.createMobAttributes());
		PolymerEntityUtils.registerType(GRAPPLE_ENTITY_TYPE, SILVERFISH_MORPH_ENTITY_TYPE, ENDERMITE_MORPH_ENTITY_TYPE)

		// Based on https://github.com/ItsRevolt/Explosive-Arrows-Fabric/blob/1.20/src/main/java/lol/shmokey/explosivearrow/ExplosiveArrow.java
		DispenserBlock.registerBehavior(EXPLOSIVE_ARROW, object : ProjectileDispenserBehavior() {
			override fun createProjectile(world: World?, position: Position, stack: ItemStack?): ProjectileEntity {
				val arrow = ArrowEntity(world, position.x, position.y, position.z, stack)
				return arrow
			}
		})

		DispenserBlock.registerBehavior(ENDER_ARROW, object : ProjectileDispenserBehavior() {
			override fun createProjectile(world: World?, position: Position, stack: ItemStack?): ProjectileEntity {
				val arrow = ArrowEntity(world, position.x, position.y, position.z, stack)
				return arrow
			}
		})

		GameType.register(Identifier.of(MOD_ID, "breach"), BreachGameConfig.CODEC, BreachWaiting::open)
	}
}