package com.nielsvoss.breachmod.config

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder

@JvmRecord
data class BreachGameConfig(val arrowsInstantKill: Boolean,
                            val map: MapConfig, val scoreNeededToWin: Int,
                            val attackerKits: AvailableKitsConfig, val defenderKits: AvailableKitsConfig,
                            val teamOptions: TeamsConfig,
                            val disableHunger: Boolean, val disableNaturalRegeneration: Boolean,
                            val disableTileDrops: Boolean, val disableFireTick: Boolean,
                            val prepLengthInSeconds: Int, val roundLengthInSeconds: Int,
                            val numberOfTargets: Int, val outlineTargets: Boolean,
                            val remainingPlayersPopup: Boolean) {
    companion object {
        @JvmStatic
        val CODEC : MapCodec<BreachGameConfig> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.BOOL.fieldOf("arrows_instant_kill").forGetter(BreachGameConfig::arrowsInstantKill),
                MapConfig.CODEC.fieldOf("map").forGetter(BreachGameConfig::map),
                Codec.INT.fieldOf("score_needed_to_win").forGetter(BreachGameConfig::scoreNeededToWin),
                AvailableKitsConfig.CODEC.fieldOf("attacker_kits").forGetter(BreachGameConfig::attackerKits),
                AvailableKitsConfig.CODEC.fieldOf("defender_kits").forGetter(BreachGameConfig::defenderKits),
                TeamsConfig.CODEC.fieldOf("teams").forGetter(BreachGameConfig::teamOptions),
                Codec.BOOL.fieldOf("disable_hunger").forGetter(BreachGameConfig::disableHunger),
                Codec.BOOL.fieldOf("disable_natural_regeneration").forGetter(BreachGameConfig::disableNaturalRegeneration),
                Codec.BOOL.fieldOf("disable_tile_drops").forGetter(BreachGameConfig::disableTileDrops),
                Codec.BOOL.fieldOf("disable_fire_tick").forGetter(BreachGameConfig::disableFireTick),
                Codec.INT.fieldOf("prep_length_in_seconds").forGetter(BreachGameConfig::prepLengthInSeconds),
                Codec.INT.fieldOf("round_length_in_seconds").forGetter(BreachGameConfig::roundLengthInSeconds),
                Codec.INT.fieldOf("number_of_targets").forGetter(BreachGameConfig::numberOfTargets),
                Codec.BOOL.fieldOf("outline_targets").forGetter(BreachGameConfig::outlineTargets),
                Codec.BOOL.fieldOf("remaining_players_popup").forGetter(BreachGameConfig::remainingPlayersPopup)
            ).apply(instance, ::BreachGameConfig)
        }
    }
}