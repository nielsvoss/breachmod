package com.nielsvoss.breachmod.item

import eu.pb4.polymer.core.api.item.PolymerItem
import eu.pb4.polymer.core.api.item.PolymerItemUtils
import net.minecraft.component.type.PotionContentsComponent
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.item.ArrowItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.potion.PotionUtil
import net.minecraft.potion.Potions
import net.minecraft.world.World
import xyz.nucleoid.packettweaker.PacketContext

class ExplosiveArrowItem(settings: Settings) : ArrowItem(settings), PolymerItem {
    override fun createArrow(world: World, stack: ItemStack, shooter: LivingEntity?, shotFrom: ItemStack?):
            PersistentProjectileEntity {
        return ArrowEntity(world, shooter, stack.copyWithCount(1), shotFrom)
    }

    override fun getPolymerItem(stack: ItemStack, context: PacketContext?): Item {
        return Items.TIPPED_ARROW
    }

    override fun getPolymerItemStack(itemStack: ItemStack, tooltipType: TooltipType?, packetContext: PacketContext?):
            ItemStack {
        val stack: ItemStack = PolymerItemUtils.createItemStack(itemStack, tooltipType, packetContext)
        // return PotionUtil.setPotion(stack, Potions.HEALING)
        return stack
    }
}
