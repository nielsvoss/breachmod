package com.nielsvoss.breachmod.data

import com.nielsvoss.breachmod.entity.AbstractMorphEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d

class MorphData private constructor(private val oldBaseMaxHealth: Double, private val oldHealth: Float, private val oldPos: Vec3d) {
    companion object {
        fun loadFrom(player: ServerPlayerEntity): MorphData {
            val oldBaseMaxHealth: Double = player.attributes.getBaseValue(EntityAttributes.GENERIC_MAX_HEALTH)
            val oldHealth: Float = player.health
            return MorphData(oldBaseMaxHealth, oldHealth, player.pos)
        }

        fun applyMorphData(player: ServerPlayerEntity, morphEntity: AbstractMorphEntity) {
            player.attributes.getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH)?.baseValue =
                morphEntity.attributes.getBaseValue(EntityAttributes.GENERIC_MAX_HEALTH)
            player.health = morphEntity.health
            // player.markHealthDirty()
            player.teleport(morphEntity.x, morphEntity.y, morphEntity.z)
        }
    }

    fun restore(player: ServerPlayerEntity) {
        player.attributes.getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH)?.baseValue = oldBaseMaxHealth
        player.health = oldHealth
        // player.markHealthDirty() - Not sure if necessary

        player.teleport(oldPos.x, oldPos.y, oldPos.z)
    }
}