package com.nielsvoss.breachmod.config

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

@JvmRecord
data class TeamsConfig(val removeTeamRestrictions: Boolean, val allowTeamChangesAfterFirstRound: Boolean,
    val randomizeFirstAttackingTeam: Boolean, val swapRolesAfterEachRound: Boolean) {
    companion object {
        val CODEC: Codec<TeamsConfig> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.fieldOf("remove_team_restrictions").forGetter(TeamsConfig::removeTeamRestrictions),
                Codec.BOOL.fieldOf("allow_team_changes_after_first_round").forGetter(TeamsConfig::allowTeamChangesAfterFirstRound),
                Codec.BOOL.fieldOf("randomize_first_attacking_team").forGetter(TeamsConfig::randomizeFirstAttackingTeam),
                Codec.BOOL.fieldOf("swap_roles_after_each_round").forGetter(TeamsConfig::swapRolesAfterEachRound)
            ).apply(instance, ::TeamsConfig)
        }
    }
}