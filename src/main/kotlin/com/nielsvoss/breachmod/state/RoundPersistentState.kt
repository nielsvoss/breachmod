package com.nielsvoss.breachmod.state

import com.nielsvoss.breachmod.data.KitSelections
import net.minecraft.scoreboard.AbstractTeam
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamConfig
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey
import xyz.nucleoid.plasmid.api.util.PlayerRef
import kotlin.math.max
import kotlin.math.min

/**
 * The state that persists between rounds of a Breach game (but not between games)
 */
class RoundPersistentState(private val scoreNeededToWin: Int, redAttacksFirst: Boolean) {
    val kitSelections = KitSelections()
    val teamState = PersistentTeamState(redAttacksFirst)

    /**
     * List of player id's that have already spawned into at least one round this game, for the purpose of deciding when
     * to increment the games played statistics (as opposed to just the rounds played statistic).
     */
    val playersThatPlayedAtLeastOneRound: MutableList<PlayerRef> = mutableListOf()

    init {
        if (scoreNeededToWin <= 0) throw IllegalArgumentException("scoreNeededToWin must be positive")
    }

    fun getScoreDisplay(): Text {
        return teamState.getScoreDisplay(scoreNeededToWin)
    }

    fun getWinningTeam(): PersistentTeamState.BreachTeam? {
        return teamState.getWinningTeam(scoreNeededToWin)
    }
}
