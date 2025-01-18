package com.nielsvoss.breachmod.config

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

@JvmRecord
data class TeamsConfig(val removeTeamRestrictions: Boolean, val allowTeamChangesAfterFirstRound: Boolean,
    val randomizeFirstAttackingTeam: Boolean, val swapRolesAfterEachRound: Boolean, val giveHelmets: Boolean) {
    companion object {
        val CODEC: Codec<TeamsConfig> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.optionalFieldOf("remove_team_restrictions", false)
                    .forGetter(TeamsConfig::removeTeamRestrictions),
                Codec.BOOL.optionalFieldOf("allow_team_changes_after_first_round", false)
                    .forGetter(TeamsConfig::allowTeamChangesAfterFirstRound),
                Codec.BOOL.optionalFieldOf("randomize_first_attacking_team", true)
                    .forGetter(TeamsConfig::randomizeFirstAttackingTeam),
                Codec.BOOL.optionalFieldOf("swap_roles_after_each_round", true)
                    .forGetter(TeamsConfig::swapRolesAfterEachRound),
                Codec.BOOL.optionalFieldOf("give_helmets", true)
                    .forGetter(TeamsConfig::giveHelmets)
            ).apply(instance, ::TeamsConfig)
        }
    }
}