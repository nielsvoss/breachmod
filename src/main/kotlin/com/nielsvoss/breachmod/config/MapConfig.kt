package com.nielsvoss.breachmod.config

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.Identifier

@JvmRecord
data class MapConfig(val id: Identifier, val timeOfDay: Long) {
    companion object {
        val CODEC: Codec<MapConfig> = RecordCodecBuilder.create { instance ->
            instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(MapConfig::id),
                Codec.LONG.fieldOf("timeOfDay").forGetter(MapConfig::timeOfDay)
            ).apply(instance, ::MapConfig)
        }
    }
}
