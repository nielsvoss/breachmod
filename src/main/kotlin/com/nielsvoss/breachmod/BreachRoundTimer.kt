package com.nielsvoss.breachmod

class BreachRoundTimer(prepPhaseLengthInTicks : Int, private val mainPhaseLengthInTicks : Int) {
    enum class Phase {
        PREP_PHASE,
        MAIN_PHASE;
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
                    phase = null
                    ticksRemainingInPhase = 0
                    return Phase.MAIN_PHASE
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

    fun displayTime(): String {
        val totalSecondsLeft = secondsRemainingInPhase()
        val min = totalSecondsLeft / 60
        val sec = totalSecondsLeft % 60
        val timeString = String.format("%02d:%02d", min, sec)
        return if (phase == Phase.PREP_PHASE) {
            "Prep: $timeString"
        } else {
            "Time: $timeString"
        }
    }
}