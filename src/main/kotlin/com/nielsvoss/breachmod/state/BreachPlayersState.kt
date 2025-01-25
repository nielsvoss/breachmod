package com.nielsvoss.breachmod.state

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import xyz.nucleoid.plasmid.api.game.GameActivity
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey
import xyz.nucleoid.plasmid.api.game.common.team.TeamManager
import xyz.nucleoid.plasmid.api.util.PlayerRef

class BreachPlayersState private constructor(private val attackingTeamKey: GameTeamKey,
                                             private val defendingTeamKey: GameTeamKey,
                                             val attackingTeamDyeColor: Int,
                                             val defendingTeamDyeColor: Int,
                                             private val teamManager: TeamManager,
                                             private val displayAttackingTeamFirst: Boolean) {
    companion object {
        fun create(activity: GameActivity, attackingTeam: GameTeam, defendingTeam: GameTeam, attackers: List<PlayerRef>, defenders: List<PlayerRef>, teamToDisplayFirst: GameTeamKey): BreachPlayersState {
            val displayAttackersFirst = when (teamToDisplayFirst) {
                attackingTeam.key -> true
                defendingTeam.key -> false
                else -> throw IllegalArgumentException("teamToDisplayFirst needs to be either the attacking or defending team")
            }

            val teamManager: TeamManager = TeamManager.addTo(activity)
            teamManager.addTeam(attackingTeam)
            teamManager.addTeam(defendingTeam)
            for (player in attackers) {
                teamManager.addPlayerTo(player, attackingTeam.key)
            }
            for (player in defenders) {
                teamManager.addPlayerTo(player, defendingTeam.key)
            }

            val breachPlayersState = BreachPlayersState(attackingTeam.key, defendingTeam.key,
                attackingTeam.config.colors.dyeColor.rgb, defendingTeam.config.colors.dyeColor.rgb,
                teamManager, displayAttackersFirst)
            return breachPlayersState
        }
    }

    private val survivingPlayers: MutableSet<PlayerRef> = mutableSetOf()

    /**
     * Includes eliminated attackers. See also isSurvivingAttacker
     */
    fun isAnyAttacker(player: ServerPlayerEntity): Boolean {
        return player in teamManager.playersIn(attackingTeamKey)
    }

    fun isAnyAttacker(playerRef: PlayerRef): Boolean {
        return playerRef in teamManager.allPlayersIn(attackingTeamKey)
    }

    /**
     * Includes eliminated defenders. See also isSurvivingDefender
     */
    fun isAnyDefender(player: ServerPlayerEntity): Boolean {
        return player in teamManager.playersIn(defendingTeamKey)
    }

    fun isAnyDefender(playerRef: PlayerRef): Boolean {
        return playerRef in teamManager.allPlayersIn(defendingTeamKey)
    }

    fun isSurvivingAttacker(player: ServerPlayerEntity): Boolean {
        return isAnyAttacker(player) && isSurviving(player)
    }

    fun isSurvivingDefender(player: ServerPlayerEntity): Boolean {
        return isAnyDefender(player) && isSurviving(player)
    }

    fun survivingOnlineAttackers(): List<ServerPlayerEntity> {
        return onlineParticipants().filter (::isSurvivingAttacker)
    }

    fun survivingOnlineDefenders(): List<ServerPlayerEntity> {
        return onlineParticipants().filter (::isSurvivingDefender)
    }

    fun survivingOnlineParticipants(): List<ServerPlayerEntity> {
        return onlineParticipants().filter (::isSurviving)
    }

    fun onlineParticipants(): List<ServerPlayerEntity> {
        val players = mutableListOf<ServerPlayerEntity>()
        players.addAll(teamManager.playersIn(attackingTeamKey))
        players.addAll(teamManager.playersIn(defendingTeamKey))
        return players
    }

    fun allParticipants(): List<PlayerRef> {
        val players = mutableListOf<PlayerRef>()
        players.addAll(teamManager.allPlayersIn(attackingTeamKey))
        players.addAll(teamManager.allPlayersIn(defendingTeamKey))
        return players
    }

    fun isSurviving(player: ServerPlayerEntity): Boolean {
        return PlayerRef.of(player) in survivingPlayers
    }

    fun eliminate(player: PlayerRef): Boolean {
        return survivingPlayers.remove(player)
    }

    fun eliminate(player: ServerPlayerEntity): Boolean {
        return eliminate(PlayerRef.of(player))
    }

    fun markSurviving(player: PlayerRef): Boolean {
        return survivingPlayers.add(player)
    }

    fun markSurviving(player: ServerPlayerEntity): Boolean {
        return markSurviving(PlayerRef.of(player))
    }

    /*
    private fun markAllOnlinePlayersAsSurviving() {
        survivingPlayers.addAll(onlineParticipants().map { PlayerRef.of(it) })
    }
     */

    fun getFirstSidebarLine(): Text {
        val team = if (displayAttackingTeamFirst) attackingTeamKey else defendingTeamKey
        val n = numSurvivingOnlinePlayers(team)
        val name = teamManager.getTeamConfig(team).name
        val nameWithColon = name.copy().append(":") // Colon gets the same color as team name
        return Text.empty().append(nameWithColon).append(Text.of(" $n"))
    }

    fun getSecondSidebarLine(): Text {
        val team = if (displayAttackingTeamFirst) defendingTeamKey else attackingTeamKey
        val n = numSurvivingOnlinePlayers(team)
        val name = teamManager.getTeamConfig(team).name
        val nameWithColon = name.copy().append(":") // Colon gets the same color as team name
        return Text.empty().append(nameWithColon).append(Text.of(" $n"))
    }

    fun getPopupMessage(): Text {
        val firstTeam = if (displayAttackingTeamFirst) attackingTeamKey else defendingTeamKey
        val secondTeam = if (displayAttackingTeamFirst) defendingTeamKey else attackingTeamKey
        val firstTeamColor: Formatting = teamManager.getTeamConfig(firstTeam).colors.chatFormatting
        val secondTeamColor: Formatting = teamManager.getTeamConfig(secondTeam).colors.chatFormatting
        val n = numSurvivingOnlinePlayers(firstTeam)
        val m = numSurvivingOnlinePlayers(secondTeam)
        return Text.empty().append(Text.literal("$n").formatted(firstTeamColor))
            .append(Text.translatable("text.breach.popup_versus"))
            .append(Text.literal("$m").formatted(secondTeamColor))
    }

    private fun numSurvivingOnlinePlayers(gameTeamKey: GameTeamKey): Int {
        val onlinePlayers = teamManager.playersIn(gameTeamKey)
        return onlinePlayers.filter(::isSurviving).size
    }
}