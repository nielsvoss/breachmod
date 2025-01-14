package com.nielsvoss.breachmod.config

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

@JvmRecord
data class TeamsConfig(val removeTeamRestrictions: Boolean, val allowTeamChangesAfterFirstRound: Boolean) {
    companion object {
        val CODEC: Codec<TeamsConfig> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.fieldOf("remove_team_restrictions").forGetter(TeamsConfig::removeTeamRestrictions),
                Codec.BOOL.fieldOf("allow_team_changes_after_first_round").forGetter(TeamsConfig::allowTeamChangesAfterFirstRound)
            ).apply(instance, ::TeamsConfig)
        }
    }
}