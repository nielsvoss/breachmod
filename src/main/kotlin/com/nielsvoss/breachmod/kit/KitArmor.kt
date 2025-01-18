package com.nielsvoss.breachmod.kit

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import xyz.nucleoid.codecs.MoreCodecs
import java.util.*

@JvmRecord
data class KitArmor(val helmet: Optional<ItemStack>, val chestplate: Optional<ItemStack>,
                    val leggings: Optional<ItemStack>, val boots: Optional<ItemStack>) {
    companion object {
        @JvmStatic
        val CODEC: Codec<KitArmor> = RecordCodecBuilder.create{ instance ->
            instance.group(
                MoreCodecs.ITEM_STACK.optionalFieldOf("helmet").forGetter(KitArmor::helmet),
                MoreCodecs.ITEM_STACK.optionalFieldOf("chestplate").forGetter(KitArmor::chestplate),
                MoreCodecs.ITEM_STACK.optionalFieldOf("leggings").forGetter(KitArmor::leggings),
                MoreCodecs.ITEM_STACK.optionalFieldOf("boots").forGetter(KitArmor::boots)
            ).apply(instance, ::KitArmor)
        }

        @JvmStatic
        val EMPTY = KitArmor(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())
    }
}