package com.nielsvoss.breachmod.config

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

@JvmRecord
data class TimesConfig(val prepLengthInSeconds: Int, val roundLengthInSeconds: Int, val secondsAfterGameEndBeforeClosure: Int) {
    companion object {
        val CODEC: Codec<TimesConfig> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.fieldOf("prep_length_in_seconds").forGetter(TimesConfig::prepLengthInSeconds),
                Codec.INT.fieldOf("round_length_in_seconds").forGetter(TimesConfig::roundLengthInSeconds),
                Codec.INT.fieldOf("seconds_after_game_end_before_closure").forGetter(TimesConfig::secondsAfterGameEndBeforeClosure)
            ).apply(instance, ::TimesConfig)
        }
    }
}