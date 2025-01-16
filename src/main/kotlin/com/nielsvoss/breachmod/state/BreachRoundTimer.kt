package com.nielsvoss.breachmod.state

class BreachRoundTimer(
    prepPhaseLengthInTicks: Int,
    private val mainPhaseLengthInTicks: Int,
    private val ticksBeforeNextRound: Int
) {
    enum class Phase {
        PREP_PHASE,
        MAIN_PHASE,
        GAME_END;
    }

    private var phase : Phase? = Phase.PREP_PHASE
    private var ticksRemainingInPhase : Int = prepPhaseLengthInTicks

    init {
        if (prepPhaseLengthInTicks <= 0 || mainPhaseLengthInTicks <= 0) {
            throw IllegalArgumentException()
        }
    }

    /**
     * If a phase just ended, returns that phase.
     */
    fun tick(): Phase? {
        if (ticksRemainingInPhase <= 1) {
            when (phase) {
                Phase.PREP_PHASE -> {
                    phase = Phase.MAIN_PHASE
                    ticksRemainingInPhase = mainPhaseLengthInTicks
                    return Phase.PREP_PHASE
                }
                Phase.MAIN_PHASE -> {
                    phase = Phase.GAME_END
                    ticksRemainingInPhase = ticksBeforeNextRound
                    return Phase.MAIN_PHASE
                }
                Phase.GAME_END -> {
                    phase = null
                    ticksRemainingInPhase = 0
                    return Phase.GAME_END
                }
                null -> {
                    ticksRemainingInPhase = 0
                    return null
                }
            }
        } else {
            ticksRemainingInPhase--
            return null
        }
    }

    private fun secondsRemainingInPhase(): Int {
        // Divide by 20, rounding up
        // We can't do (ticks - 1) / 20 + 1 because java integer division rounds negative numbers upwards, so if
        // ticks = 0 it returns 1
        return (ticksRemainingInPhase + 20 - 1) / 20
    }

    fun setGameEnd() {
        ticksRemainingInPhase = ticksBeforeNextRound
        phase = Phase.GAME_END
    }

    fun displayTime(): String {
        val totalSecondsLeft = secondsRemainingInPhase()
        val min = totalSecondsLeft / 60
        val sec = totalSecondsLeft % 60
        val timeString = String.format("%02d:%02d", min, sec)
        // TODO: Localization
        return if (phase == Phase.PREP_PHASE) {
            "Prep: $timeString"
        } else if (phase == Phase.GAME_END) {
            "Ending: $timeString"
        } else {
            "Time: $timeString"
        }
    }

    fun isPrepPhase(): Boolean {
        return phase == Phase.PREP_PHASE
    }

    fun isGameEnded(): Boolean {
        return phase == Phase.GAME_END || phase == null
    }
}