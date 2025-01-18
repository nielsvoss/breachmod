package com.nielsvoss.breachmod.data

import com.nielsvoss.breachmod.entity.AbstractMorphEntity
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d

class MorphData private constructor(
    private val oldBaseMaxHealth: Double,
    private val oldHealth: Float,
    private val oldPos: Vec3d,
    private val oldWasInvisible: Boolean,
    private val oldWasInvulnerable: Boolean,
    private val oldWasSilent: Boolean,
    private val oldScale: Double
) {
    companion object {
        fun loadFrom(player: ServerPlayerEntity): MorphData {
            val oldBaseMaxHealth: Double = player.attributes.getBaseValue(EntityAttributes.MAX_HEALTH)
            val oldHealth: Float = player.health
            val oldWasInvisible = player.isInvisible
            val oldWasInvulnerable = player.isInvulnerable
            val oldWasSilent = player.isSilent
            val oldScale = player.attributes.getBaseValue(EntityAttributes.SCALE)
            return MorphData(
                oldBaseMaxHealth,
                oldHealth,
                player.pos,
                oldWasInvisible,
                oldWasInvulnerable,
                oldWasSilent,
                oldScale
            )
        }

        fun applyMorphData(player: ServerPlayerEntity, morphEntity: AbstractMorphEntity) {
            // TODO for 1.21 - Adjust player size scale
            val morphEntityWorld = morphEntity.world
            if (!morphEntityWorld.isClient && morphEntityWorld is ServerWorld) {
                player.attributes.getCustomInstance(EntityAttributes.MAX_HEALTH)?.baseValue =
                    morphEntity.attributes.getBaseValue(EntityAttributes.MAX_HEALTH)
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

                player.attributes.getCustomInstance(EntityAttributes.SCALE)?.baseValue = 0.3

                player.teleport(
                    morphEntityWorld, morphEntity.x, morphEntity.y, morphEntity.z, setOf(),
                    morphEntity.yaw, morphEntity.pitch, true
                )
            }
        }
    }

    fun restore(player: ServerPlayerEntity) {
        player.attributes.getCustomInstance(EntityAttributes.MAX_HEALTH)?.baseValue = oldBaseMaxHealth
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

        player.attributes.getCustomInstance(EntityAttributes.SCALE)?.baseValue = oldScale

        // TODO: Store yaw and pitch
        player.teleport(player.serverWorld, oldPos.x, oldPos.y, oldPos.z, setOf(),
            0F, 0F, true)
    }
}