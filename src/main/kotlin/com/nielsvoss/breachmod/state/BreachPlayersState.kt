package com.nielsvoss.breachmod.state

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import xyz.nucleoid.plasmid.game.GameActivity
import xyz.nucleoid.plasmid.game.common.team.GameTeam
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey
import xyz.nucleoid.plasmid.game.common.team.TeamManager
import xyz.nucleoid.plasmid.util.PlayerRef

class BreachPlayersState private constructor(private val attackingTeamKey: GameTeamKey,
                                             private val defendingTeamKey: GameTeamKey,
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

            val breachPlayersState = BreachPlayersState(attackingTeam.key, defendingTeam.key, teamManager, displayAttackersFirst)
            breachPlayersState.markAllOnlinePlayersAsSurviving()
            return breachPlayersState
        }
    }

    private val survivingPlayers: MutableSet<PlayerRef> = mutableSetOf()

    fun survivingOnlineAttackers(): List<ServerPlayerEntity> {
        return survivingOnlineParticipants().filter {
            it in teamManager.playersIn(attackingTeamKey)
        }
    }

    fun survivingOnlineDefenders(): List<ServerPlayerEntity> {
        return survivingOnlineParticipants().filter {
            it in teamManager.playersIn(defendingTeamKey)
        }
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

    private fun markAllOnlinePlayersAsSurviving() {
        survivingPlayers.addAll(onlineParticipants().map { PlayerRef.of(it) })
    }

    fun getFirstSidebarLine(): Text {
        val team = if (displayAttackingTeamFirst) attackingTeamKey else defendingTeamKey
        val n = numSurvivingOnlinePlayers(team)
        return teamManager.getTeamConfig(team).name.copy().append(Text.of(": $n"))
    }

    fun getSecondSidebarLine(): Text {
        val team = if (displayAttackingTeamFirst) defendingTeamKey else attackingTeamKey
        val n = numSurvivingOnlinePlayers(team)
        return teamManager.getTeamConfig(team).name.copy().append(Text.of(": $n"))
    }

    private fun numSurvivingOnlinePlayers(gameTeamKey: GameTeamKey): Int {
        val onlinePlayers = teamManager.playersIn(gameTeamKey)
        return onlinePlayers.filter(::isSurviving).size
    }
}