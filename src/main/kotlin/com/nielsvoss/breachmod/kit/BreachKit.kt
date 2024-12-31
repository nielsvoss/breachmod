package com.nielsvoss.breachmod.kit

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import xyz.nucleoid.codecs.MoreCodecs
import xyz.nucleoid.plasmid.api.util.ItemStackBuilder

/**
 * Originally based on https://github.com/NucleoidMC/skywars/blob/main/src/main/java/us/potatoboy/skywars/kit/Kit.java
 */
@JvmRecord
data class BreachKit(val items: List<ItemStack>) {
    companion object {
        @JvmStatic
        val CODEC: Codec<BreachKit> = RecordCodecBuilder.create{ instance ->
            instance.group(Codec.list(MoreCodecs.ITEM_STACK).fieldOf("items").forGetter(BreachKit::items))
                .apply(instance, ::BreachKit)
        }
    }

    fun equipPlayer(player: ServerPlayerEntity) {
        for (stack in items) {
            player.inventory.insertStack(ItemStackBuilder.of(stack).build())
        }
    }
}
