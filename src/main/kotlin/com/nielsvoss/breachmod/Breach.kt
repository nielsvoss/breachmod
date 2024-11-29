package com.nielsvoss.breachmod

import com.nielsvoss.breachmod.game.BreachWaiting
import com.nielsvoss.breachmod.item.ExplosiveArrowItem
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.DispenserBlock
import net.minecraft.block.dispenser.ProjectileDispenserBehavior
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

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "explosive_arrow"), EXPLOSIVE_ARROW)

		// Based on https://github.com/ItsRevolt/Explosive-Arrows-Fabric/blob/1.20/src/main/java/lol/shmokey/explosivearrow/ExplosiveArrow.java
		DispenserBlock.registerBehavior(EXPLOSIVE_ARROW, object : ProjectileDispenserBehavior() {
			override fun createProjectile(world: World?, position: Position, stack: ItemStack?): ProjectileEntity {
				val arrow = ArrowEntity(world, position.x, position.y, position.z, stack)
				return arrow
			}
		})

		GameType.register(Identifier.of(MOD_ID, "breach"), BreachGameConfig.CODEC, BreachWaiting::open)
	}
}