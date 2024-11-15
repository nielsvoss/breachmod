package com.nielsvoss.breachmod

import com.nielsvoss.breachmod.mixin.BlockDisplayEntityAccessor
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityType
import net.minecraft.entity.decoration.DisplayEntity
import net.minecraft.entity.decoration.DisplayEntity.BlockDisplayEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
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
        }
    }

    fun updateOutlines(world: ServerWorld) {
        for (target in selectedTargets) {
            if (target !in brokenTargets) {
                if (outlineEntities[target]?.isAlive != true) {
                    outlineEntities.remove(target)
                }

                if (target !in outlineEntities) {
                    val blockState: BlockState = world.getBlockState(target.pos)
                    val blockDisplayEntity = createBlockDisplayEntity(world, blockState, target.pos)
                    outlineEntities[target] = blockDisplayEntity
                }
            } else {
                outlineEntities.remove(target)?.kill()
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

    private fun createBlockDisplayEntity(world: ServerWorld, blockState: BlockState, pos: BlockPos): BlockDisplayEntity {
        val display = BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world)
        display.setPosition(Vec3d(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()))
        display.isGlowing = true
        display.isInvisible = true // Doesn't seem to do anything right now

        @Suppress("KotlinConstantConditions")
        (display as BlockDisplayEntityAccessor).invokeSetBlockState(blockState)

        world.spawnEntity(display)
        return display
    }
}