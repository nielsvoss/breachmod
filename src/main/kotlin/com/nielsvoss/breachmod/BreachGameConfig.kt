package com.nielsvoss.breachmod

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

@JvmRecord
data class BreachGameConfig(val killArrows: Boolean) {
    companion object {
        @JvmStatic
        val CODEC : Codec<BreachGameConfig> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.fieldOf("killArrows").forGetter(BreachGameConfig::killArrows)
            ).apply(instance, ::BreachGameConfig)
        }
    }
}