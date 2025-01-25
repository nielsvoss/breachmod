package com.nielsvoss.breachmod

import net.minecraft.util.Identifier
import xyz.nucleoid.plasmid.api.game.stats.StatisticKey

object BreachStatistics {
    val ROUNDS_PLAYED: StatisticKey<Int> = StatisticKey.intKey(Identifier.of(Breach.MOD_ID, "rounds_played"))
}