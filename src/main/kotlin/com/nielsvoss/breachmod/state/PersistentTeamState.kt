package com.nielsvoss.breachmod.state

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

/*
 * In the future, this class can be made generic, the generic type being essentially Fin n = {0, 1, ... n}, so that we
 * can have n teams
 */
class PersistentTeamState(redAttacksFirst: Boolean) {
    private val redTeam = createTeam("BreachRed", Text.translatable("team.breach.red"), DyeColor.RED)
    private val blueTeam = createTeam("BreachBlue", Text.translatable("team.breach.blue"), DyeColor.BLUE)

    private val redTeamMemberList: MutableList<PlayerRef> = mutableListOf()
    private val blueTeamMemberList: MutableList<PlayerRef> = mutableListOf()

    private val teamToDisplayFirst: BreachTeam = BreachTeam.RED

    private var redTeamScore: Int = 0
    private var blueTeamScore: Int = 0

    private var attackingTeam: BreachTeam = if (redAttacksFirst) BreachTeam.RED else BreachTeam.BLUE

    fun getAttackingTeam(): BreachTeam = attackingTeam
    fun getDefendingTeam(): BreachTeam = attackingTeam.getOtherTeam()

    fun getTeamToDisplayFirst(): BreachTeam = teamToDisplayFirst
    fun getTeamToDisplaySecond(): BreachTeam = teamToDisplayFirst.getOtherTeam()

    fun swapRoles() {
        attackingTeam = attackingTeam.getOtherTeam()
    }

    fun getGameTeam(team: BreachTeam): GameTeam {
        return when (team) {
            BreachTeam.RED -> redTeam
            BreachTeam.BLUE -> blueTeam
        }
    }

    fun getScore(team: BreachTeam): Int {
        return when (team) {
            BreachTeam.RED -> redTeamScore
            BreachTeam.BLUE -> blueTeamScore
        }
    }

    fun incrementScore(team: BreachTeam) {
        when (team) {
            BreachTeam.RED -> redTeamScore++
            BreachTeam.BLUE -> blueTeamScore++
        }
    }

    fun getMemberList(team: BreachTeam): MutableList<PlayerRef> {
        return when (team) {
            BreachTeam.RED -> redTeamMemberList
            BreachTeam.BLUE -> blueTeamMemberList
        }
    }

    fun getWinningTeam(scoreNeededToWin: Int): BreachTeam? {
        return if (redTeamScore >= scoreNeededToWin) BreachTeam.RED
        else if (blueTeamScore >= scoreNeededToWin) BreachTeam.BLUE
        else null
    }

    fun removeFromAllTeams(playerRef: PlayerRef) {
        redTeamMemberList.remove(playerRef)
        blueTeamMemberList.remove(playerRef)
    }

    fun getScoreDisplay(scoreNeededToWin: Int): Text {
        val rightUnfilledTriangle = "\u25B7"
        val rightFilledTriangle = "\u25B6"
        val leftUnfilledTriangle = "\u25C1"
        val leftFilledTriangle = "\u25C0"
        val unfilledCircle = "\u25CB"
        val filledCircle = "\u23FA"

        val leftTeam = getTeamToDisplayFirst()
        val rightTeam = getTeamToDisplaySecond()
        val leftGameTeam = getGameTeam(leftTeam)
        val rightGameTeam = getGameTeam(rightTeam)
        val leftTeamScore: Int = getScore(leftTeam)
        val rightTeamScore: Int = getScore(rightTeam)

        val leftFilledArrows: MutableText =
            Text.literal(rightFilledTriangle.repeat(min(leftTeamScore, scoreNeededToWin - 1)))
                .formatted(leftGameTeam.config.chatFormatting())
        val leftUnfilledArrows: Text =
            Text.literal(rightUnfilledTriangle.repeat(max(0, scoreNeededToWin - 1 - leftTeamScore)))
                .formatted(leftGameTeam.config.chatFormatting())
        val centerCircle: Text =
            if (leftTeamScore >= scoreNeededToWin) Text.literal(filledCircle).formatted(leftGameTeam.config.chatFormatting())
            else if (rightTeamScore >= scoreNeededToWin) Text.literal(filledCircle).formatted(rightGameTeam.config.chatFormatting())
            else Text.literal(unfilledCircle).formatted(Formatting.WHITE)
        val rightUnfilledArrows: Text =
            Text.literal(leftUnfilledTriangle.repeat(max(0, scoreNeededToWin - 1 - rightTeamScore)))
                .formatted(rightGameTeam.config.chatFormatting())
        val rightFilledArrows: Text =
            Text.literal(leftFilledTriangle.repeat(min(rightTeamScore, scoreNeededToWin - 1)))
                .formatted(rightGameTeam.config.chatFormatting())

        return leftFilledArrows.append(leftUnfilledArrows).append(centerCircle)
            .append(rightUnfilledArrows).append(rightFilledArrows)
    }

    enum class BreachTeam {
        RED, BLUE;

        fun getOtherTeam(): BreachTeam {
            return when (this) {
                RED -> BLUE
                BLUE -> RED
            }
        }
    }
}

private fun createTeam(id: String, name: Text, color: DyeColor): GameTeam {
    return GameTeam(
        GameTeamKey(id),
        GameTeamConfig.builder()
            .setCollision(AbstractTeam.CollisionRule.NEVER)
            .setNameTagVisibility(AbstractTeam.VisibilityRule.NEVER)
            .setColors(GameTeamConfig.Colors.from(color))
            .setName(name)
            .build())
}
