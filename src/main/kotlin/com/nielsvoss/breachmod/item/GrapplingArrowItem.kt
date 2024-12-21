package com.nielsvoss.breachmod.item

import com.nielsvoss.breachmod.entity.GrappleEntity
import com.nielsvoss.breachmod.PersistentProjectileEntityDuck
import eu.pb4.polymer.core.api.item.PolymerItem
import eu.pb4.polymer.core.api.item.PolymerItemUtils
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.item.ArrowItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.potion.PotionUtil
import net.minecraft.potion.Potions
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World

class GrapplingArrowItem(settings: Settings) : ArrowItem(settings), PolymerItem {
    override fun createArrow(world: World, stack: ItemStack, shooter: LivingEntity?): PersistentProjectileEntity {
        val arrow = ArrowEntity(world, shooter, stack.copyWithCount(1))
        if (!world.isClient && world is ServerWorld && shooter is ServerPlayerEntity) {
            val grapple: GrappleEntity = GrappleEntity.create(world, arrow, shooter)
            PersistentProjectileEntityDuck.setGrapple(arrow, grapple) // TODO: This should be done in the GrappleEntity class
        }
        return arrow
    }

    override fun getPolymerItem(stack: ItemStack, player: ServerPlayerEntity?): Item {
        return Items.TIPPED_ARROW
    }

    override fun getPolymerItemStack(itemStack: ItemStack, context: TooltipContext, player: ServerPlayerEntity?): ItemStack {
        val stack: ItemStack = PolymerItemUtils.createItemStack(itemStack, context, player)
        return PotionUtil.setPotion(stack, Potions.LEAPING)
    }
}
