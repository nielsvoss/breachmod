package com.nielsvoss.breachmod.data

import net.minecraft.scoreboard.AbstractTeam
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamConfig
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey

/**
 * The state that persists between rounds of a Breach game (but not between games)
 */
class RoundPersistentState {
    val kitSelections = KitSelections()
    val team1 = createTeam("Breach1", Text.translatable("team.breach.red"), DyeColor.RED)
    val team2 = createTeam("Breach2", Text.translatable("team.breach.blue"), DyeColor.BLUE)
    private var isTeam1Attacking: Boolean = true
    private val displayTeam1First: Boolean = true

    fun getAttackingTeam(): GameTeam = if (isTeam1Attacking) team1 else team2
    fun getDefendingTeam(): GameTeam = if (isTeam1Attacking) team2 else team1
    fun getTeamToDisplayFirst(): GameTeam = if (displayTeam1First) team1 else team2
    fun getTeamToDisplaySecond(): GameTeam = if (displayTeam1First) team2 else team1
    fun swapRoles() {
        isTeam1Attacking = !isTeam1Attacking
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
