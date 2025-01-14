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
                Codec.BOOL.fieldOf("arrows_instant_kill").forGetter(GameplayOptionsConfig::arrowsInstantKill),
                Codec.BOOL.fieldOf("disable_hunger").forGetter(GameplayOptionsConfig::disableHunger),
                Codec.BOOL.fieldOf("disable_natural_regeneration").forGetter(GameplayOptionsConfig::disableNaturalRegeneration),
                Codec.BOOL.fieldOf("disable_tile_drops").forGetter(GameplayOptionsConfig::disableTileDrops),
                Codec.BOOL.fieldOf("disable_fire_tick").forGetter(GameplayOptionsConfig::disableFireTick)
            ).apply(instance, ::GameplayOptionsConfig)
        }
    }
}