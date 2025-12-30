package com.nielsvoss.breachmod.item

import eu.pb4.polymer.core.api.item.PolymerItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Identifier
import xyz.nucleoid.packettweaker.PacketContext

class TeleportSelectorItem(settings: Settings) : Item(settings), PolymerItem {
    override fun getPolymerItem(p0: ItemStack?, p1: PacketContext?): Item {
        return Items.ENDER_EYE
    }

    override fun getPolymerItemModel(stack: ItemStack?, context: PacketContext?): Identifier? {
        return null
    }
}