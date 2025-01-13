package com.nielsvoss.breachmod.kit

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.Identifier

@JvmRecord
data class AvailableKitsConfig(val kits: List<Identifier>, val categories: List<String>) {
    companion object {
        @JvmStatic
        val CODEC: Codec<AvailableKitsConfig> = RecordCodecBuilder.create{ instance ->
            instance.group(
                Codec.list(Identifier.CODEC).fieldOf("kits").forGetter(AvailableKitsConfig::kits),
                Codec.list(Codec.STRING).fieldOf("categories").forGetter(AvailableKitsConfig::categories)
            ).apply(instance, ::AvailableKitsConfig)
        }
    }

    fun getKits(): List<BreachKit> {
        return BreachKitRegistry.getKits(kits, categories)
    }
}
