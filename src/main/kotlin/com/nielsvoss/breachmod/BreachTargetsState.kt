package com.nielsvoss.breachmod

import net.minecraft.server.world.ServerWorld

class BreachTargetsState(private val availableTargets: List<BreachTarget>) {
    private val selectedTargets: MutableList<BreachTarget> = mutableListOf()
    private val brokenTargets: MutableSet<BreachTarget> = mutableSetOf()

    fun selectTarget(target: BreachTarget) {
        if (target !in availableTargets) {
            throw IllegalArgumentException("Tried to select target not in list of available targets")
        }
        selectedTargets.add(target)
    }

    fun updateBrokenTargets(world: ServerWorld) {
        // TODO: Consider whether it should be selectedTargets or availableTargets
        for (target in selectedTargets) {
            if (target !in brokenTargets && world.getBlockState(target.pos).block != target.block) {
                brokenTargets.add(target)
            }
        }
    }

    fun populate(size: Int) {
        if (selectedTargets.size >= size) return

        val unselectedTargets: MutableList<BreachTarget> = availableTargets.toMutableList()
        unselectedTargets.removeAll(selectedTargets)
        unselectedTargets.shuffle()
        var i = 0
        while (selectedTargets.size < size && i < unselectedTargets.size) {
            selectedTargets.add(unselectedTargets[i])
            i++
        }
    }

    fun selected(): List<BreachTarget> {
        return selectedTargets
    }

    fun isBroken(target: BreachTarget): Boolean {
        return target in brokenTargets
    }

    override fun toString(): String {
        return "BreachTargetsState{available: $availableTargets, selected: $selectedTargets, broken: $brokenTargets}"
    }
}