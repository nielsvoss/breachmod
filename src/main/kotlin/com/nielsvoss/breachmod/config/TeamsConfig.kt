package com.nielsvoss.breachmod.config

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

@JvmRecord
data class TeamsConfig(val removeTeamRestrictions: Boolean, val allowTeamChangesAfterFirstRound: Boolean,
    val randomizeFirstAttackingTeam: Boolean, val swapRolesAfterEachRound: Boolean, val giveHelmets: Boolean) {
    companion object {
        @JvmStatic
        val DEFAULT: TeamsConfig = TeamsConfig(
            removeTeamRestrictions = false,
            allowTeamChangesAfterFirstRound = false,
            randomizeFirstAttackingTeam = true,
            swapRolesAfterEachRound = true,
            giveHelmets = true
        )

        @JvmStatic
        val CODEC: Codec<TeamsConfig> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.optionalFieldOf("remove_team_restrictions", DEFAULT.removeTeamRestrictions)
                    .forGetter(TeamsConfig::removeTeamRestrictions),
                Codec.BOOL.optionalFieldOf("allow_team_changes_after_first_round", DEFAULT.allowTeamChangesAfterFirstRound)
                    .forGetter(TeamsConfig::allowTeamChangesAfterFirstRound),
                Codec.BOOL.optionalFieldOf("randomize_first_attacking_team", DEFAULT.randomizeFirstAttackingTeam)
                    .forGetter(TeamsConfig::randomizeFirstAttackingTeam),
                Codec.BOOL.optionalFieldOf("swap_roles_after_each_round", DEFAULT.swapRolesAfterEachRound)
                    .forGetter(TeamsConfig::swapRolesAfterEachRound),
                Codec.BOOL.optionalFieldOf("give_helmets", DEFAULT.giveHelmets)
                    .forGetter(TeamsConfig::giveHelmets)
            ).apply(instance, ::TeamsConfig)
        }
    }
}