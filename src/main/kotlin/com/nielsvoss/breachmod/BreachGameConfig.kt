package com.nielsvoss.breachmod

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

@JvmRecord
data class BreachGameConfig(val arrowsInstantKill: Boolean, val prepLengthInSeconds: Int, val roundLengthInSeconds: Int,
                            val numberOfTargets: Int, val outlineTargets: Boolean) {
    companion object {
        @JvmStatic
        val CODEC : Codec<BreachGameConfig> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.fieldOf("arrowsInstantKill").forGetter(BreachGameConfig::arrowsInstantKill),
                Codec.INT.fieldOf("prepLengthInSeconds").forGetter(BreachGameConfig::prepLengthInSeconds),
                Codec.INT.fieldOf("roundLengthInSeconds").forGetter(BreachGameConfig::roundLengthInSeconds),
                Codec.INT.fieldOf("numberOfTargets").forGetter(BreachGameConfig::numberOfTargets),
                Codec.BOOL.fieldOf("outlineTargets").forGetter(BreachGameConfig::outlineTargets)
            ).apply(instance, ::BreachGameConfig)
        }
    }
}