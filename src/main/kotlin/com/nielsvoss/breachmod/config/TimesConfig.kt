package com.nielsvoss.breachmod.config

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder

@JvmRecord
data class TimesConfig(
    val prepLengthInSeconds: Int,
    val roundLengthInSeconds: Int,
    val lobbyReadySeconds: Int,
    val lobbyFullSeconds: Int,
    val secondsAfterRoundEndBeforeNext: Int,
    val secondsAfterGameEndBeforeClosure: Int
) {
    companion object {
        val CODEC: Codec<TimesConfig> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.optionalFieldOf("prep_length_in_seconds", 30).forGetter(TimesConfig::prepLengthInSeconds),
                Codec.INT.optionalFieldOf("round_length_in_seconds", 180).forGetter(TimesConfig::roundLengthInSeconds),
                Codec.INT.optionalFieldOf("lobby_ready_seconds", 30).forGetter(TimesConfig::lobbyReadySeconds),
                Codec.INT.optionalFieldOf("lobby_full_seconds", 10).forGetter(TimesConfig::lobbyFullSeconds),
                Codec.INT.optionalFieldOf("seconds_after_round_end_before_next", 10).forGetter(TimesConfig::secondsAfterRoundEndBeforeNext),
                Codec.INT.optionalFieldOf("seconds_after_game_end_before_closure", 10).forGetter(TimesConfig::secondsAfterGameEndBeforeClosure)
            ).apply(instance, ::TimesConfig)
        }

        @JvmStatic
        val DEFAULT: TimesConfig = CODEC.parse(JsonOps.INSTANCE, JsonObject()).resultOrPartial().orElseThrow()
    }
}