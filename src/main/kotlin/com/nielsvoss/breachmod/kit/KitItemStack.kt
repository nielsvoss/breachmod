package com.nielsvoss.breachmod.kit

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.item.ItemStack
import xyz.nucleoid.codecs.MoreCodecs
import java.util.*
import java.util.function.Function

/**
 * ItemStack with extra optional metadata to control how it behaves in the kit.
 */
@JvmRecord
data class KitItemStack(val stack: ItemStack, val slot: Optional<Int>) {
    companion object {
        private fun fromItemStack(stack: ItemStack): KitItemStack {
            return KitItemStack(stack, Optional.empty())
        }

        private val simpleCodec: Codec<KitItemStack> = RecordCodecBuilder.create { instance ->
            instance.group(
                MoreCodecs.ITEM_STACK.fieldOf("stack").forGetter(KitItemStack::stack),
                Codec.INT.optionalFieldOf("slot").forGetter(KitItemStack::slot)
            ).apply(instance, ::KitItemStack)
        }

        val CODEC: Codec<KitItemStack> = Codec.either(simpleCodec, MoreCodecs.ITEM_STACK)
            .xmap({ either -> either.map(Function.identity(), ::fromItemStack) }) { Either.left(it) }
    }
}