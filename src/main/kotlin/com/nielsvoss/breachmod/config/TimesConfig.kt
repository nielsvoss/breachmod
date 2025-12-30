package com.nielsvoss.breachmod.config

import com.mojang.serialization.Codec
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
        @JvmStatic
        val DEFAULT: TimesConfig = TimesConfig(
            prepLengthInSeconds = 15,
            roundLengthInSeconds = 120,
            lobbyReadySeconds = 30,
            lobbyFullSeconds = 10,
            secondsAfterRoundEndBeforeNext = 10,
            secondsAfterGameEndBeforeClosure = 10
        )

        @JvmStatic
        val CODEC: Codec<TimesConfig> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.INT.optionalFieldOf("prep_length_in_seconds", DEFAULT.prepLengthInSeconds)
                    .forGetter(TimesConfig::prepLengthInSeconds),
                Codec.INT.optionalFieldOf("round_length_in_seconds", DEFAULT.roundLengthInSeconds)
                    .forGetter(TimesConfig::roundLengthInSeconds),
                Codec.INT.optionalFieldOf("lobby_ready_seconds", DEFAULT.lobbyReadySeconds)
                    .forGetter(TimesConfig::lobbyReadySeconds),
                Codec.INT.optionalFieldOf("lobby_full_seconds", DEFAULT.lobbyFullSeconds)
                    .forGetter(TimesConfig::lobbyFullSeconds),
                Codec.INT.optionalFieldOf("seconds_after_round_end_before_next", DEFAULT.secondsAfterRoundEndBeforeNext)
                    .forGetter(TimesConfig::secondsAfterRoundEndBeforeNext),
                Codec.INT.optionalFieldOf("seconds_after_game_end_before_closure", DEFAULT.secondsAfterGameEndBeforeClosure)
                    .forGetter(TimesConfig::secondsAfterGameEndBeforeClosure)
            ).apply(instance, ::TimesConfig)
        }
    }
}