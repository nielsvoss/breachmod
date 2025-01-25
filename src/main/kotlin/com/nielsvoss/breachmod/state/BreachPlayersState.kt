package com.nielsvoss.breachmod.state

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting
import xyz.nucleoid.plasmid.api.game.GameActivity
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey
import xyz.nucleoid.plasmid.api.game.common.team.TeamManager
import xyz.nucleoid.plasmid.api.util.PlayerRef

class BreachPlayersState private constructor(private val teamManager: TeamManager,
                                             private val teamState: PersistentTeamState) {
    companion object {
        fun create(activity: GameActivity, teamState: PersistentTeamState, participants: List<ServerPlayerEntity>):
                BreachPlayersState {
            val teamManager: TeamManager = TeamManager.addTo(activity)
            teamManager.addTeam(teamState.getGameTeam(PersistentTeamState.BreachTeam.RED))
            teamManager.addTeam(teamState.getGameTeam(PersistentTeamState.BreachTeam.BLUE))

            for (player in participants) {
                if (PlayerRef.of(player) in teamState.getMemberList(PersistentTeamState.BreachTeam.RED))
                    teamManager.addPlayerTo(player, teamState.getGameTeam(PersistentTeamState.BreachTeam.RED).key)
                else if (PlayerRef.of(player) in teamState.getMemberList(PersistentTeamState.BreachTeam.BLUE))
                    teamManager.addPlayerTo(player, teamState.getGameTeam(PersistentTeamState.BreachTeam.BLUE).key)
            }

            val breachPlayersState = BreachPlayersState(teamManager, teamState)
            return breachPlayersState
        }
    }

    private val survivingPlayers: MutableSet<PlayerRef> = mutableSetOf()

    private val attackingTeamKey = teamState.getGameTeam(teamState.getAttackingTeam()).key
    private val defendingTeamKey = teamState.getGameTeam(teamState.getDefendingTeam()).key

    val attackingTeamDyeColor: TextColor = teamState.getGameTeam(teamState.getAttackingTeam()).config.colors.dyeColor
    val defendingTeamDyeColor: TextColor = teamState.getGameTeam(teamState.getDefendingTeam()).config.colors.dyeColor

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
        val team = teamState.getGameTeam(teamState.getTeamToDisplayFirst()).key
        val n = numSurvivingOnlinePlayers(team)
        val name = teamManager.getTeamConfig(team).name
        val nameWithColon = name.copy().append(":") // Colon gets the same color as team name
        return Text.empty().append(nameWithColon).append(Text.of(" $n"))
    }

    fun getSecondSidebarLine(): Text {
        val team = teamState.getGameTeam(teamState.getTeamToDisplaySecond()).key
        val n = numSurvivingOnlinePlayers(team)
        val name = teamManager.getTeamConfig(team).name
        val nameWithColon = name.copy().append(":") // Colon gets the same color as team name
        return Text.empty().append(nameWithColon).append(Text.of(" $n"))
    }

    fun getPopupMessage(): Text {
        val firstTeam = teamState.getGameTeam(teamState.getTeamToDisplayFirst()).key
        val secondTeam = teamState.getGameTeam(teamState.getTeamToDisplaySecond()).key
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