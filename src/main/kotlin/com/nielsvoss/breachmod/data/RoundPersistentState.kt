package com.nielsvoss.breachmod.data

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
class RoundPersistentState(private val scoreNeededToWin: Int, private var isTeam1Attacking: Boolean) {
    val kitSelections = KitSelections()
    val team1 = createTeam("Breach1", Text.translatable("team.breach.red"), DyeColor.RED)
    val team2 = createTeam("Breach2", Text.translatable("team.breach.blue"), DyeColor.BLUE)
    private var team1Score: Int = 0
    private var team2Score: Int = 0
    private val displayTeam1First: Boolean = true

    val team1Members: MutableList<PlayerRef> = mutableListOf()
    val team2Members: MutableList<PlayerRef> = mutableListOf()

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
        val rightUnfilledTriangle = "\u25B7"
        val rightFilledTriangle = "\u25B6"
        val leftUnfilledTriangle = "\u25C1"
        val leftFilledTriangle = "\u25C0"
        val unfilledCircle = "\u25CB"
        val filledCircle = "\u23FA"

        val leftTeam = if (displayTeam1First) team1 else team2
        val rightTeam = if (displayTeam1First) team2 else team1
        val leftTeamScore: Int = if (displayTeam1First) team1Score else team2Score
        val rightTeamScore: Int = if (displayTeam1First) team2Score else team1Score

        val leftFilledArrows: MutableText =
            Text.literal(rightFilledTriangle.repeat(min(leftTeamScore, scoreNeededToWin - 1)))
                .formatted(leftTeam.config.chatFormatting())
        val leftUnfilledArrows: Text =
            Text.literal(rightUnfilledTriangle.repeat(max(0, scoreNeededToWin - 1 - leftTeamScore)))
                .formatted(leftTeam.config.chatFormatting())
        val centerCircle: Text =
            if (leftTeamScore >= scoreNeededToWin) Text.literal(filledCircle).formatted(leftTeam.config.chatFormatting())
            else if (rightTeamScore >= scoreNeededToWin) Text.literal(filledCircle).formatted(rightTeam.config.chatFormatting())
            else Text.literal(unfilledCircle).formatted(Formatting.WHITE)
        val rightUnfilledArrows: Text =
            Text.literal(leftUnfilledTriangle.repeat(max(0, scoreNeededToWin - 1 - rightTeamScore)))
                .formatted(rightTeam.config.chatFormatting())
        val rightFilledArrows: Text =
            Text.literal(leftFilledTriangle.repeat(min(rightTeamScore, scoreNeededToWin - 1)))
                .formatted(rightTeam.config.chatFormatting())

        return leftFilledArrows.append(leftUnfilledArrows).append(centerCircle)
            .append(rightUnfilledArrows).append(rightFilledArrows)
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
