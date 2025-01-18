package com.nielsvoss.breachmod.kit

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import xyz.nucleoid.codecs.MoreCodecs

@JvmRecord
data class KitArmor(val helmet: ItemStack, val chestplate: ItemStack, val leggings: ItemStack, val boots: ItemStack) {
    companion object {
        @JvmStatic
        val CODEC: Codec<KitArmor> = RecordCodecBuilder.create{ instance ->
            instance.group(
                MoreCodecs.ITEM_STACK.optionalFieldOf("helmet", ItemStack(Items.AIR)).forGetter(KitArmor::helmet),
                MoreCodecs.ITEM_STACK.optionalFieldOf("chestplate", ItemStack(Items.AIR)).forGetter(KitArmor::chestplate),
                MoreCodecs.ITEM_STACK.optionalFieldOf("leggings", ItemStack(Items.AIR)).forGetter(KitArmor::leggings),
                MoreCodecs.ITEM_STACK.optionalFieldOf("boots", ItemStack(Items.AIR)).forGetter(KitArmor::boots)
            ).apply(instance, ::KitArmor)
        }
    }
}