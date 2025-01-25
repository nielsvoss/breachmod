package com.nielsvoss.breachmod

import net.minecraft.util.Identifier
import xyz.nucleoid.plasmid.api.game.stats.StatisticKey

object BreachStatistics {
    val ROUNDS_PLAYED: StatisticKey<Int> = StatisticKey.intKey(Identifier.of(Breach.MOD_ID, "rounds_played"))
    val ROUNDS_WON: StatisticKey<Int> = StatisticKey.intKey(Identifier.of(Breach.MOD_ID, "rounds_won"))
    val ROUNDS_LOST: StatisticKey<Int> = StatisticKey.intKey(Identifier.of(Breach.MOD_ID, "rounds_lost"))
    val ROUNDS_TIED: StatisticKey<Int> = StatisticKey.intKey(Identifier.of(Breach.MOD_ID, "rounds_tied"))
}