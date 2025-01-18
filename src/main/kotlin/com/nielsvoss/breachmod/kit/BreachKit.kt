package com.nielsvoss.breachmod.kit

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import xyz.nucleoid.codecs.MoreCodecs
import xyz.nucleoid.plasmid.api.util.ItemStackBuilder

/**
 * Originally based on https://github.com/NucleoidMC/skywars/blob/main/src/main/java/us/potatoboy/skywars/kit/Kit.java
 */
@JvmRecord
data class BreachKit(val nameTranslationKey: String, private val icon: ItemStack, val items: List<ItemStack>,
                     val armor: KitArmor, val categories: List<String>) {
    companion object {
        @JvmStatic
        val CODEC: Codec<BreachKit> = RecordCodecBuilder.create{ instance ->
            instance.group(
                Codec.STRING.fieldOf("name").forGetter(BreachKit::nameTranslationKey),
                MoreCodecs.ITEM_STACK.fieldOf("icon").forGetter(BreachKit::icon),
                Codec.list(MoreCodecs.ITEM_STACK).fieldOf("items").forGetter(BreachKit::items),
                KitArmor.CODEC.fieldOf("armor").forGetter(BreachKit::armor),
                Codec.list(Codec.STRING).fieldOf("categories").forGetter(BreachKit::categories)
            ).apply(instance, ::BreachKit)
        }
    }

    fun equipPlayer(player: ServerPlayerEntity) {
        for (stack in items) {
            player.inventory.insertStack(ItemStackBuilder.of(stack).build())
        }

        if (!armor.helmet.isOf(Items.AIR)) player.equipStack(EquipmentSlot.HEAD, armor.helmet.copy())
        if (!armor.chestplate.isOf(Items.AIR)) player.equipStack(EquipmentSlot.BODY, armor.chestplate.copy())
        if (!armor.leggings.isOf(Items.AIR)) player.equipStack(EquipmentSlot.LEGS, armor.leggings.copy())
        if (!armor.boots.isOf(Items.AIR)) player.equipStack(EquipmentSlot.FEET, armor.boots.copy())
    }

    fun getIcon(): ItemStack {
        return icon.copy()
    }

    fun getName(): Text {
        return Text.translatable(nameTranslationKey)
    }
}
