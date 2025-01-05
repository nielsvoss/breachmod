package com.nielsvoss.breachmod.item

import eu.pb4.polymer.core.api.item.PolymerItem
import eu.pb4.polymer.core.api.item.PolymerItemUtils
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.PotionContentsComponent
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.item.ArrowItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.util.Identifier
import net.minecraft.world.World
import xyz.nucleoid.packettweaker.PacketContext
import java.util.*

class EnderArrowItem(settings: Settings) : ArrowItem(settings), PolymerItem {
    override fun createArrow(world: World, stack: ItemStack, shooter: LivingEntity?, shotFrom: ItemStack?): PersistentProjectileEntity {
        return ArrowEntity(world, shooter, stack.copyWithCount(1), shotFrom)
    }

    override fun getPolymerItem(stack: ItemStack, context: PacketContext?): Item {
        return Items.TIPPED_ARROW
    }

    override fun getPolymerItemModel(stack: ItemStack?, context: PacketContext?): Identifier? {
        return null
    }

    override fun modifyBasePolymerItemStack(out: ItemStack, stack: ItemStack?, context: PacketContext?) {
        val color = 0x004000 // dark green
        val potion = PotionContentsComponent(Optional.empty(), Optional.of(color), listOf(), Optional.of("breach_ender_arrow"))
        out.set(DataComponentTypes.POTION_CONTENTS, potion)
    }
}
