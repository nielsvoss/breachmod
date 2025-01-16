package com.nielsvoss.breachmod.game

import com.nielsvoss.breachmod.config.BreachGameConfig
import com.nielsvoss.breachmod.data.RoundPersistentState
import com.nielsvoss.breachmod.util.sendSubtitle
import com.nielsvoss.breachmod.util.sendTitle
import com.nielsvoss.breachmod.util.setTitleTimes
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.world.GameMode
import xyz.nucleoid.plasmid.api.game.GameCloseReason
import xyz.nucleoid.plasmid.api.game.GameSpace
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType

class BreachVictory private constructor(
    private val gameSpace: GameSpace,
    private val world: ServerWorld,
    private val winningTeam: GameTeam,
    private var ticksBeforeClosure: Int
) {
    companion object {
        fun open(gameSpace: GameSpace, world: ServerWorld, config: BreachGameConfig,
                 persistentState: RoundPersistentState, winningTeam: GameTeam
        ): BreachVictory {
            val ticksBeforeClosure = config.timesConfig.secondsAfterGameEndBeforeClosure * 20
            val breachVictory = BreachVictory(gameSpace, world, winningTeam, ticksBeforeClosure)

            gameSpace.setActivity { activity ->
                activity.deny(GameRuleType.PORTALS)

                activity.listen(GameActivityEvents.ENABLE, GameActivityEvents.Enable { breachVictory.open() })
                activity.listen(GameActivityEvents.TICK, GameActivityEvents.Tick { breachVictory.tick() })
           }

            return breachVictory
        }
    }

    fun open() {
        for (player in world.players) {
            player.changeGameMode(GameMode.SPECTATOR)
            player.setTitleTimes(0, 60, 20)
            player.sendTitle(Text.translatable("text.breach.game_over"))
            player.sendSubtitle(winningTeam.config.name.copy().append(Text.translatable("text.breach.has_won")))
        }
    }

    fun tick() {
        if (ticksBeforeClosure <= 0) {
            gameSpace.close(GameCloseReason.FINISHED)
        } else {
            ticksBeforeClosure--
        }
    }
}