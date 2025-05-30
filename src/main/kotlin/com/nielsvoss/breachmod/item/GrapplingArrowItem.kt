package com.nielsvoss.breachmod.item

import com.nielsvoss.breachmod.PersistentProjectileEntityDuck
import com.nielsvoss.breachmod.entity.GrappleEntity
import eu.pb4.polymer.core.api.item.PolymerItem
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.PotionContentsComponent
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.item.ArrowItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.world.World
import xyz.nucleoid.packettweaker.PacketContext
import java.util.*

class GrapplingArrowItem(settings: Settings) : ArrowItem(settings), PolymerItem {
    override fun createArrow(world: World, stack: ItemStack, shooter: LivingEntity?, shotFrom: ItemStack?): PersistentProjectileEntity {
        val arrow = ArrowEntity(world, shooter, stack.copyWithCount(1), null)
        if (!world.isClient && world is ServerWorld && shooter is ServerPlayerEntity) {
            val grapple: GrappleEntity = GrappleEntity.create(world, arrow, shooter)
            PersistentProjectileEntityDuck.setGrapple(arrow, grapple) // TODO: This should be done in the GrappleEntity class
            arrow.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED
        }
        return arrow
    }

    override fun getPolymerItem(stack: ItemStack, context: PacketContext?): Item {
        return Items.TIPPED_ARROW
    }

    override fun getPolymerItemModel(stack: ItemStack?, context: PacketContext?): Identifier? {
        return null
    }

    override fun modifyBasePolymerItemStack(out: ItemStack, stack: ItemStack?, context: PacketContext?) {
        val color = 0x804000 // brown
        val potion = PotionContentsComponent(Optional.empty(), Optional.of(color), listOf(), Optional.of("breach_grappling_arrow"))
        out.set(DataComponentTypes.POTION_CONTENTS, potion)
    }
}
