package com.nielsvoss.breachmod

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import com.nielsvoss.breachmod.kit.AvailableKitsConfig
import net.minecraft.util.Identifier

@JvmRecord
data class BreachGameConfig(val arrowsInstantKill: Boolean,
                            val map: Identifier, val timeOfDay: Long, val scoreNeededToWin: Int,
                            val attackerKits: AvailableKitsConfig, val defenderKits: AvailableKitsConfig,
                            val disableHunger: Boolean, val disableNaturalRegeneration: Boolean,
                            val disableTileDrops: Boolean, val disableFireTick: Boolean,
                            val prepLengthInSeconds: Int, val roundLengthInSeconds: Int,
                            val numberOfTargets: Int, val outlineTargets: Boolean,
                            val remainingPlayersPopup: Boolean) {
    companion object {
        @JvmStatic
        val CODEC : MapCodec<BreachGameConfig> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.BOOL.fieldOf("arrowsInstantKill").forGetter(BreachGameConfig::arrowsInstantKill),
                Identifier.CODEC.fieldOf("map").forGetter(BreachGameConfig::map),
                Codec.LONG.fieldOf("timeOfDay").forGetter(BreachGameConfig::timeOfDay),
                Codec.INT.fieldOf("scoreNeededToWin").forGetter(BreachGameConfig::scoreNeededToWin),
                AvailableKitsConfig.CODEC.fieldOf("attackerKits").forGetter(BreachGameConfig::attackerKits),
                AvailableKitsConfig.CODEC.fieldOf("defenderKits").forGetter(BreachGameConfig::defenderKits),
                Codec.BOOL.fieldOf("disableHunger").forGetter(BreachGameConfig::disableHunger),
                Codec.BOOL.fieldOf("disableNaturalRegeneration").forGetter(BreachGameConfig::disableNaturalRegeneration),
                Codec.BOOL.fieldOf("disableTileDrops").forGetter(BreachGameConfig::disableTileDrops),
                Codec.BOOL.fieldOf("disableFireTick").forGetter(BreachGameConfig::disableFireTick),
                Codec.INT.fieldOf("prepLengthInSeconds").forGetter(BreachGameConfig::prepLengthInSeconds),
                Codec.INT.fieldOf("roundLengthInSeconds").forGetter(BreachGameConfig::roundLengthInSeconds),
                Codec.INT.fieldOf("numberOfTargets").forGetter(BreachGameConfig::numberOfTargets),
                Codec.BOOL.fieldOf("outlineTargets").forGetter(BreachGameConfig::outlineTargets),
                Codec.BOOL.fieldOf("remainingPlayersPopup").forGetter(BreachGameConfig::remainingPlayersPopup)
            ).apply(instance, ::BreachGameConfig)
        }
    }
}