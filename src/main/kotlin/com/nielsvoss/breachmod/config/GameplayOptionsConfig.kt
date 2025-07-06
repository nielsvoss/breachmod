package com.nielsvoss.breachmod.config

import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder

@JvmRecord
data class GameplayOptionsConfig(
    val arrowsInstantKill: Boolean, val disableHunger: Boolean, val disableNaturalRegeneration: Boolean,
    val disableTileDrops: Boolean, val disableFireTick: Boolean, val beaconsRevealPlayersTime: Int,
    val endRoundWhenATeamIsEliminated: Boolean
) {
    companion object {
        @JvmStatic
        val DEFAULT: GameplayOptionsConfig = GameplayOptionsConfig(
            arrowsInstantKill = true,
            disableHunger = true,
            disableNaturalRegeneration = true,
            disableTileDrops = true,
            disableFireTick = true,
            beaconsRevealPlayersTime = 200,
            endRoundWhenATeamIsEliminated = true
        )

        @JvmStatic
        val CODEC: Codec<GameplayOptionsConfig> = RecordCodecBuilder.create{ instance ->
            instance.group(
                Codec.BOOL.optionalFieldOf("arrows_instant_kill", DEFAULT.arrowsInstantKill)
                    .forGetter(GameplayOptionsConfig::arrowsInstantKill),
                Codec.BOOL.optionalFieldOf("disable_hunger", DEFAULT.disableHunger)
                    .forGetter(GameplayOptionsConfig::disableHunger),
                Codec.BOOL.optionalFieldOf("disable_natural_regeneration", DEFAULT.disableNaturalRegeneration)
                    .forGetter(GameplayOptionsConfig::disableNaturalRegeneration),
                Codec.BOOL.optionalFieldOf("disable_tile_drops", DEFAULT.disableTileDrops)
                    .forGetter(GameplayOptionsConfig::disableTileDrops),
                Codec.BOOL.optionalFieldOf("disable_fire_tick", DEFAULT.disableFireTick)
                    .forGetter(GameplayOptionsConfig::disableFireTick),
                Codec.INT.optionalFieldOf("beacons_reveal_players_time", DEFAULT.beaconsRevealPlayersTime)
                    .forGetter(GameplayOptionsConfig::beaconsRevealPlayersTime),
                Codec.BOOL.optionalFieldOf("end_round_when_a_team_is_eliminated", DEFAULT.endRoundWhenATeamIsEliminated)
                    .forGetter(GameplayOptionsConfig::endRoundWhenATeamIsEliminated)
            ).apply(instance, ::GameplayOptionsConfig)
        }
    }
}