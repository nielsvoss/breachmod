package com.nielsvoss.breachmod

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

@JvmRecord
data class BreachGameConfig(val arrowsInstantKill: Boolean) {
    companion object {
        @JvmStatic
        val CODEC : Codec<BreachGameConfig> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.fieldOf("arrowsInstantKill").forGetter(BreachGameConfig::arrowsInstantKill)
            ).apply(instance, ::BreachGameConfig)
        }
    }
}