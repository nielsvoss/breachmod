package com.nielsvoss.breachmod.util

import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.DyedColorComponent
import net.minecraft.component.type.UnbreakableComponent
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity

object TeamArmorUtils {
    fun giveTeamArmor(player: ServerPlayerEntity, color: Int, includeHelmet: Boolean) {
        val helmet = ItemStack(Items.LEATHER_HELMET)
        helmet.set(DataComponentTypes.UNBREAKABLE, UnbreakableComponent(true))
        helmet.set(DataComponentTypes.DYED_COLOR, DyedColorComponent(color, false))

        val chestplate = ItemStack(Items.LEATHER_CHESTPLATE)
        chestplate.set(DataComponentTypes.UNBREAKABLE, UnbreakableComponent(true))
        chestplate.set(DataComponentTypes.DYED_COLOR, DyedColorComponent(color, false))

        val leggings = ItemStack(Items.LEATHER_LEGGINGS)
        leggings.set(DataComponentTypes.UNBREAKABLE, UnbreakableComponent(true))
        leggings.set(DataComponentTypes.DYED_COLOR, DyedColorComponent(color, false))

        val boots = ItemStack(Items.LEATHER_BOOTS)
        boots.set(DataComponentTypes.UNBREAKABLE, UnbreakableComponent(true))
        boots.set(DataComponentTypes.DYED_COLOR, DyedColorComponent(color, false))

        if (includeHelmet) player.equipStack(EquipmentSlot.HEAD, helmet)
        player.equipStack(EquipmentSlot.CHEST, chestplate)
        player.equipStack(EquipmentSlot.LEGS, leggings)
        player.equipStack(EquipmentSlot.FEET, boots)
    }

    fun clearArmor(player: ServerPlayerEntity, removeHelmet: Boolean) {
        if (removeHelmet) player.equipStack(EquipmentSlot.HEAD, ItemStack(Items.AIR))
        player.equipStack(EquipmentSlot.CHEST, ItemStack(Items.AIR))
        player.equipStack(EquipmentSlot.LEGS, ItemStack(Items.AIR))
        player.equipStack(EquipmentSlot.FEET, ItemStack(Items.AIR))
    }
}