package com.nielsvoss.breachmod

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import xyz.nucleoid.plasmid.game.GameSpace
import xyz.nucleoid.plasmid.game.common.team.GameTeam
import xyz.nucleoid.plasmid.game.common.team.TeamManager
import xyz.nucleoid.plasmid.game.event.GameActivityEvents
import xyz.nucleoid.plasmid.util.PlayerRef

class BreachActive private constructor(private val gameSpace: GameSpace, private val world: ServerWorld,
                   private val config: BreachGameConfig, private val attackingTeam: GameTeam,
                   private val defendingTeam: GameTeam, private val teamManager: TeamManager) {
    companion object {
        fun open(gameSpace: GameSpace, world: ServerWorld, config: BreachGameConfig, attackingTeam: GameTeam,
                 defendingTeam: GameTeam, attackers: List<PlayerRef>, defenders: List<PlayerRef>) {
            gameSpace.setActivity { activity ->
                val teamManager: TeamManager = TeamManager.addTo(activity)
                teamManager.addTeam(attackingTeam)
                teamManager.addTeam(defendingTeam)
                for (player in attackers) {
                    teamManager.addPlayerTo(player, attackingTeam.key)
                }
                for (player in defenders) {
                    teamManager.addPlayerTo(player, defendingTeam.key)
                }

                val breachActive = BreachActive(gameSpace, world, config, attackingTeam, defendingTeam, teamManager)

                activity.listen(GameActivityEvents.TICK, GameActivityEvents.Tick { breachActive.tick() })
            }
        }
    }

    val roundTimer: BreachRoundTimer
    init {
        var prepTicks = config.prepLengthInSeconds * 20;
        var roundTicks = config.roundLengthInSeconds * 20;
        if (prepTicks <= 0) {
            broadcastServerError("Configuration Error: prepLengthInSeconds was a nonpositve number")
            prepTicks = 1
        }
        if (roundTicks <= 0) {
            broadcastServerError("Configuration Error: roundLengthInSeconds was a nonpositive number")
            roundTicks = 1
        }
        roundTimer = BreachRoundTimer(prepTicks, roundTicks)
    }

    private fun tick() {
        val phaseThatJustEnded: BreachRoundTimer.Phase? = roundTimer.tick()
        if (phaseThatJustEnded != null) {
            broadcast(Text.literal("Phase just ended"))
        }
    }

    private fun onlineParticipants(): List<ServerPlayerEntity> {
        val players = mutableListOf<ServerPlayerEntity>()
        players.addAll(teamManager.playersIn(attackingTeam.key))
        players.addAll(teamManager.playersIn(defendingTeam.key))
        return players
    }

    private fun allParticipants(): List<PlayerRef> {
        val players = mutableListOf<PlayerRef>()
        players.addAll(teamManager.allPlayersIn(attackingTeam.key))
        players.addAll(teamManager.allPlayersIn(defendingTeam.key))
        return players
    }

    private fun broadcast(text: Text) {
        onlineParticipants().forEach { it.sendMessage(text) }
    }

    private fun broadcastServerError(message: String) {
        broadcast(Text.literal(message))
        broadcast(Text.literal("Please contact the server admins."))
    }
}