package com.nielsvoss.breachmod.data

import com.nielsvoss.breachmod.entity.AbstractMorphEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d

class MorphData private constructor(
    private val oldBaseMaxHealth: Double,
    private val oldHealth: Float,
    private val oldPos: Vec3d,
    private val oldWasInvisible: Boolean,
    private val oldWasInvulnerable: Boolean,
    private val oldWasSilent: Boolean
) {
    companion object {
        fun loadFrom(player: ServerPlayerEntity): MorphData {
            val oldBaseMaxHealth: Double = player.attributes.getBaseValue(EntityAttributes.GENERIC_MAX_HEALTH)
            val oldHealth: Float = player.health
            val oldWasInvisible = player.isInvisible
            val oldWasInvulnerable = player.isInvulnerable
            val oldWasSilent = player.isSilent
            return MorphData(
                oldBaseMaxHealth,
                oldHealth,
                player.pos,
                oldWasInvulnerable,
                oldWasInvulnerable,
                oldWasSilent
            )
        }

        fun applyMorphData(player: ServerPlayerEntity, morphEntity: AbstractMorphEntity) {
            // TODO for 1.21 - Adjust player size scale

            player.attributes.getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH)?.baseValue =
                morphEntity.attributes.getBaseValue(EntityAttributes.GENERIC_MAX_HEALTH)
            player.health = morphEntity.health
            // player.markHealthDirty()
            if (!player.isInvisible) {
                player.isInvisible = true
            }
            if (!player.isInvulnerable) {
                player.isInvulnerable = true
            }
            if (!player.isSilent) {
                player.isSilent = true
            }
            player.teleport(morphEntity.x, morphEntity.y, morphEntity.z)
        }
    }

    fun restore(player: ServerPlayerEntity) {
        player.attributes.getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH)?.baseValue = oldBaseMaxHealth
        player.health = oldHealth
        // player.markHealthDirty() - Not sure if necessary

        if (player.isInvisible != oldWasInvisible) {
            player.isInvisible = oldWasInvisible
        }
        if (player.isInvulnerable != oldWasInvulnerable) {
            player.isInvulnerable = oldWasInvulnerable
        }
        if (player.isSilent != oldWasSilent) {
            player.isSilent = oldWasSilent
        }

        player.teleport(oldPos.x, oldPos.y, oldPos.z)
    }
}