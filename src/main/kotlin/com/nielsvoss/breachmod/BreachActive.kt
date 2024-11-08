package com.nielsvoss.breachmod

import net.minecraft.server.world.ServerWorld
import xyz.nucleoid.plasmid.game.GameSpace
import xyz.nucleoid.plasmid.game.common.team.GameTeam
import xyz.nucleoid.plasmid.game.common.team.TeamManager
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
            }
        }
    }
}