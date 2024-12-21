package com.nielsvoss.breachmod.item

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
import net.minecraft.world.World

class GrapplingArrowItem(settings: Settings) : ArrowItem(settings), PolymerItem {
    override fun createArrow(world: World, stack: ItemStack, shooter: LivingEntity?): PersistentProjectileEntity {
        return ArrowEntity(world, shooter, stack.copyWithCount(1))
    }

    override fun getPolymerItem(stack: ItemStack, player: ServerPlayerEntity?): Item {
        return Items.TIPPED_ARROW
    }

    override fun getPolymerItemStack(itemStack: ItemStack, context: TooltipContext, player: ServerPlayerEntity?): ItemStack {
        val stack: ItemStack = PolymerItemUtils.createItemStack(itemStack, context, player)
        return PotionUtil.setPotion(stack, Potions.LEAPING)
    }
}
