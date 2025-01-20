package com.nielsvoss.breachmod.config

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.nielsvoss.breachmod.kit.BreachKit
import com.nielsvoss.breachmod.kit.BreachKitRegistry
import net.minecraft.util.Identifier

@JvmRecord
data class AvailableKitsConfig(val kits: List<Identifier>, val categories: List<String>) {
    companion object {
        val ATTACKER_DEFAULT = AvailableKitsConfig(listOf(), listOf("attacker"))
        val DEFENDER_DEFAULT = AvailableKitsConfig(listOf(), listOf("defender"))

        @JvmStatic
        val CODEC: Codec<AvailableKitsConfig> = RecordCodecBuilder.create{ instance ->
            instance.group(
                Codec.list(Identifier.CODEC).optionalFieldOf("kits", listOf()).forGetter(AvailableKitsConfig::kits),
                Codec.list(Codec.STRING).fieldOf("categories").forGetter(AvailableKitsConfig::categories)
            ).apply(instance, ::AvailableKitsConfig)
        }
    }

    fun getKits(): List<BreachKit> {
        return BreachKitRegistry.getKits(kits, categories)
    }
}
