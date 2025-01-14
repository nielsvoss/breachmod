package com.nielsvoss.breachmod.config

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

@JvmRecord
data class TeamsConfig(val removeTeamRestrictions: Boolean, val allowTeamChangesAfterFirstRound: Boolean) {
    companion object {
        val CODEC: Codec<TeamsConfig> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.fieldOf("removeTeamRestrictions").forGetter(TeamsConfig::removeTeamRestrictions),
                Codec.BOOL.fieldOf("allowTeamChangesAfterFirstRound").forGetter(TeamsConfig::allowTeamChangesAfterFirstRound)
            ).apply(instance, ::TeamsConfig)
        }
    }
}