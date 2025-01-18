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
                MapConfig.CODEC.fieldOf("map")
                    .forGetter(BreachGameConfig::map),
                Codec.INT.optionalFieldOf("score_needed_to_win", 3)
                    .forGetter(BreachGameConfig::scoreNeededToWin),
                AvailableKitsConfig.CODEC.optionalFieldOf("attacker_kits", AvailableKitsConfig.ATTACKER_DEFAULT)
                    .forGetter(BreachGameConfig::attackerKits),
                AvailableKitsConfig.CODEC.optionalFieldOf("defender_kits", AvailableKitsConfig.DEFENDER_DEFAULT)
                    .forGetter(BreachGameConfig::defenderKits),
                TeamsConfig.CODEC.fieldOf("teams")
                    .forGetter(BreachGameConfig::teamOptions),
                GameplayOptionsConfig.CODEC.fieldOf("gameplay")
                    .forGetter(BreachGameConfig::gameplayOptions),
                TimesConfig.CODEC.fieldOf("times")
                    .forGetter(BreachGameConfig::timesConfig),
                Codec.INT.optionalFieldOf("number_of_targets", 2)
                    .forGetter(BreachGameConfig::numberOfTargets),
                Codec.BOOL.optionalFieldOf("outline_targets", true)
                    .forGetter(BreachGameConfig::outlineTargets),
                Codec.BOOL.optionalFieldOf("remaining_players_popup", true)
                    .forGetter(BreachGameConfig::remainingPlayersPopup)
            ).apply(instance, ::BreachGameConfig)
        }
    }
}