package com.nielsvoss.breachmod

import eu.pb4.sidebars.api.Sidebar
import eu.pb4.sidebars.api.lines.SuppliedSidebarLine
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class Round private constructor(participants : List<ServerPlayerEntity>,
                                prepLengthInSeconds : Int, roundLengthInSeconds : Int) {
    private val time: RoundTime
    private val sidebar: Sidebar = Sidebar(Sidebar.Priority.MEDIUM)
    private val participants: MutableList<ServerPlayerEntity> = participants.toMutableList()

    companion object {
        @JvmStatic
        fun startNew(participants: List<ServerPlayerEntity>, prepLengthInSeconds : Int, roundLengthInSeconds : Int): Round {
            val new = Round(participants, prepLengthInSeconds, roundLengthInSeconds)
            new.start()
            return new
        }
    }

    init {
        val prepTicks = prepLengthInSeconds * 20
        val roundTicks = roundLengthInSeconds * 20
        time = RoundTime(prepTicks, roundTicks)

        sidebar.title = Text.literal("Breach")
        sidebar.setLine(SuppliedSidebarLine(0) { Text.literal(time.displayTime()) })
    }

    private fun start() {
        sidebar.show()
    }

    fun tick() {
        val phaseThatJustFinished : RoundTime.Phase? = time.tick()
    }

    fun addParticipant(player : ServerPlayerEntity) {
        if (!participants.contains(player)) {
            participants.add(player)
            sidebar.addPlayer(player)
        }
    }
}