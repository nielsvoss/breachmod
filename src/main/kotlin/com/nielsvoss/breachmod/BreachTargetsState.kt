package com.nielsvoss.breachmod

import com.nielsvoss.breachmod.mixin.BlockDisplayEntityAccessor
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityType
import net.minecraft.entity.decoration.DisplayEntity
import net.minecraft.entity.decoration.DisplayEntity.BlockDisplayEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d

class BreachTargetsState(private val availableTargets: List<BreachTarget>) {
    private val selectedTargets: MutableList<BreachTarget> = mutableListOf()
    private val brokenTargets: MutableSet<BreachTarget> = mutableSetOf()
    private val outlineEntities: MutableMap<BreachTarget, DisplayEntity> = mutableMapOf()

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

            if (outlineEntities[target]?.isAlive != true) {
                outlineEntities.remove(target)
            }

            if (target !in outlineEntities) {
                val display = BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world)
                val pos = Vec3d(target.pos.x.toDouble(), target.pos.y.toDouble(), target.pos.z.toDouble())
                val blockState: BlockState = world.getBlockState(target.pos)
                (display as BlockDisplayEntityAccessor).invokeSetBlockState(blockState)
                display.setPosition(pos)
                display.isGlowing = true
                display.isInvisible = true
                world.spawnEntity(display)
                outlineEntities[target] = display
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