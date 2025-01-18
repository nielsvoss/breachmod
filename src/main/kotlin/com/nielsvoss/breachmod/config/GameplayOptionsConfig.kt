package com.nielsvoss.breachmod.config

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

@JvmRecord
data class GameplayOptionsConfig(
    val arrowsInstantKill: Boolean, val disableHunger: Boolean, val disableNaturalRegeneration: Boolean,
    val disableTileDrops: Boolean, val disableFireTick: Boolean,
) {
    companion object {
        @JvmStatic
        val CODEC: Codec<GameplayOptionsConfig> = RecordCodecBuilder.create{ instance ->
            instance.group(
                Codec.BOOL.optionalFieldOf("arrows_instant_kill", true).forGetter(GameplayOptionsConfig::arrowsInstantKill),
                Codec.BOOL.optionalFieldOf("disable_hunger", true).forGetter(GameplayOptionsConfig::disableHunger),
                Codec.BOOL.optionalFieldOf("disable_natural_regeneration", true).forGetter(GameplayOptionsConfig::disableNaturalRegeneration),
                Codec.BOOL.optionalFieldOf("disable_tile_drops", true).forGetter(GameplayOptionsConfig::disableTileDrops),
                Codec.BOOL.optionalFieldOf("disable_fire_tick", true).forGetter(GameplayOptionsConfig::disableFireTick)
            ).apply(instance, ::GameplayOptionsConfig)
        }
    }
}