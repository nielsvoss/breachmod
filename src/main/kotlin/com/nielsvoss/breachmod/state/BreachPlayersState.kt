package com.nielsvoss.breachmod.state

import net.minecraft.server.network.ServerPlayerEntity
import xyz.nucleoid.plasmid.game.GameActivity
import xyz.nucleoid.plasmid.game.common.team.GameTeam
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey
import xyz.nucleoid.plasmid.game.common.team.TeamManager
import xyz.nucleoid.plasmid.util.PlayerRef

class BreachPlayersState private constructor(private val attackingTeamKey: GameTeamKey,
                                             private val defendingTeamKey: GameTeamKey,
                                             private val teamManager: TeamManager) {
    companion object {
        fun create(activity: GameActivity, attackingTeam: GameTeam, defendingTeam: GameTeam, attackers: List<PlayerRef>, defenders: List<PlayerRef>): BreachPlayersState {
            val teamManager: TeamManager = TeamManager.addTo(activity)
            teamManager.addTeam(attackingTeam)
            teamManager.addTeam(defendingTeam)
            for (player in attackers) {
                teamManager.addPlayerTo(player, attackingTeam.key)
            }
            for (player in defenders) {
                teamManager.addPlayerTo(player, defendingTeam.key)
            }

            val breachPlayersState = BreachPlayersState(attackingTeam.key, defendingTeam.key, teamManager)
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
}