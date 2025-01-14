package com.nielsvoss.breachmod.data

import net.minecraft.scoreboard.AbstractTeam
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import xyz.nucleoid.plasmid.api.game.GameOpenException
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamConfig
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey
import xyz.nucleoid.plasmid.api.util.PlayerRef

/**
 * The state that persists between rounds of a Breach game (but not between games)
 */
class RoundPersistentState(private val scoreNeededToWin: Int) {
    val kitSelections = KitSelections()
    val team1 = createTeam("Breach1", Text.translatable("team.breach.red"), DyeColor.RED)
    val team2 = createTeam("Breach2", Text.translatable("team.breach.blue"), DyeColor.BLUE)
    var team1Score: Int = 0
    var team2Score: Int = 0
    private var isTeam1Attacking: Boolean = true
    private val displayTeam1First: Boolean = true

    private val team1Members: MutableList<PlayerRef> = mutableListOf()
    private val team2Members: MutableList<PlayerRef> = mutableListOf()

    init {
        if (scoreNeededToWin <= 0) throw IllegalArgumentException("scoreNeededToWin must be positive")
    }

    fun getAttackingTeam(): GameTeam = if (isTeam1Attacking) team1 else team2
    fun getDefendingTeam(): GameTeam = if (isTeam1Attacking) team2 else team1
    fun getAttackingTeamMembers(): MutableList<PlayerRef> = if (isTeam1Attacking) team1Members else team2Members
    fun getDefendingTeamMembers(): MutableList<PlayerRef> = if (isTeam1Attacking) team2Members else team1Members
    fun getTeamToDisplayFirst(): GameTeam = if (displayTeam1First) team1 else team2
    fun getTeamToDisplaySecond(): GameTeam = if (displayTeam1First) team2 else team1
    fun swapRoles() {
        isTeam1Attacking = !isTeam1Attacking
    }

    fun incrementAttackerScore() {
        if (isTeam1Attacking) team1Score++ else team2Score++
    }

    fun incrementDefenderScore() {
        if (isTeam1Attacking) team2Score++ else team1Score++
    }

    fun getScoreDisplay(): Text {
        return Text.of("Current score: $team1Score - $team2Score")
    }

    fun getWinningTeam(): GameTeam? {
        return if (team1Score >= scoreNeededToWin) team1
        else if (team2Score >= scoreNeededToWin) team2
        else null
    }
}

private fun createTeam(id: String, name: Text, color: DyeColor): GameTeam {
    return GameTeam(
        GameTeamKey(id),
        GameTeamConfig.builder()
            .setCollision(AbstractTeam.CollisionRule.NEVER)
            .setColors(GameTeamConfig.Colors.from(color))
            .setName(name)
            .build())
}
