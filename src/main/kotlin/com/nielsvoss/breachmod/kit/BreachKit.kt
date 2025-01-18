package com.nielsvoss.breachmod.kit

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import xyz.nucleoid.codecs.MoreCodecs
import java.util.*

/**
 * Originally based on https://github.com/NucleoidMC/skywars/blob/main/src/main/java/us/potatoboy/skywars/kit/Kit.java
 */
@JvmRecord
data class BreachKit(val nameTranslationKey: String, private val icon: ItemStack, val items: List<ItemStack>,
                     val armor: KitArmor, val offhand: Optional<ItemStack>, val categories: List<String>) {
    companion object {
        @JvmStatic
        val CODEC: Codec<BreachKit> = RecordCodecBuilder.create{ instance ->
            instance.group(
                Codec.STRING.fieldOf("name").forGetter(BreachKit::nameTranslationKey),
                MoreCodecs.ITEM_STACK.fieldOf("icon").forGetter(BreachKit::icon),
                Codec.list(MoreCodecs.ITEM_STACK).fieldOf("items").forGetter(BreachKit::items),
                KitArmor.CODEC.optionalFieldOf("armor", KitArmor.EMPTY).forGetter(BreachKit::armor),
                MoreCodecs.ITEM_STACK.optionalFieldOf("offhand").forGetter(BreachKit::offhand),
                Codec.list(Codec.STRING).fieldOf("categories").forGetter(BreachKit::categories)
            ).apply(instance, ::BreachKit)
        }
    }

    fun equipPlayer(player: ServerPlayerEntity) {
        for ((i, stack) in items.withIndex()) {
            player.inventory.insertStack(i, stack.copy())
        }

        if (armor.helmet.isPresent) player.equipStack(EquipmentSlot.HEAD, armor.helmet.get().copy())
        if (armor.chestplate.isPresent) player.equipStack(EquipmentSlot.BODY, armor.chestplate.get().copy())
        if (armor.leggings.isPresent) player.equipStack(EquipmentSlot.LEGS, armor.leggings.get().copy())
        if (armor.boots.isPresent) player.equipStack(EquipmentSlot.FEET, armor.boots.get().copy())

        if (offhand.isPresent) player.equipStack(EquipmentSlot.OFFHAND, offhand.get().copy())
    }

    fun getIcon(): ItemStack {
        return icon.copy()
    }

    fun getName(): Text {
        return Text.translatable(nameTranslationKey)
    }
}
