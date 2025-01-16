package com.nielsvoss.breachmod.config

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder

@JvmRecord
data class BreachGameConfig(val map: MapConfig, val scoreNeededToWin: Int,
                            val attackerKits: AvailableKitsConfig, val defenderKits: AvailableKitsConfig,
                            val teamOptions: TeamsConfig, val gameplayOptions: GameplayOptionsConfig,
                            val timesConfig: TimesConfig,
                            val numberOfTargets: Int, val outlineTargets: Boolean,
                            val remainingPlayersPopup: Boolean) {
    companion object {
        @JvmStatic
        val CODEC : MapCodec<BreachGameConfig> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                MapConfig.CODEC.fieldOf("map").forGetter(BreachGameConfig::map),
                Codec.INT.fieldOf("score_needed_to_win").forGetter(BreachGameConfig::scoreNeededToWin),
                AvailableKitsConfig.CODEC.fieldOf("attacker_kits").forGetter(BreachGameConfig::attackerKits),
                AvailableKitsConfig.CODEC.fieldOf("defender_kits").forGetter(BreachGameConfig::defenderKits),
                TeamsConfig.CODEC.fieldOf("teams").forGetter(BreachGameConfig::teamOptions),
                GameplayOptionsConfig.CODEC.fieldOf("gameplay").forGetter(BreachGameConfig::gameplayOptions),
                TimesConfig.CODEC.fieldOf("times").forGetter(BreachGameConfig::timesConfig),
                Codec.INT.fieldOf("number_of_targets").forGetter(BreachGameConfig::numberOfTargets),
                Codec.BOOL.fieldOf("outline_targets").forGetter(BreachGameConfig::outlineTargets),
                Codec.BOOL.fieldOf("remaining_players_popup").forGetter(BreachGameConfig::remainingPlayersPopup)
            ).apply(instance, ::BreachGameConfig)
        }
    }
}