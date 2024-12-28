package com.nielsvoss.breachmod.entity

import eu.pb4.polymer.core.api.entity.PolymerEntity
import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.MobEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World
import kotlin.math.max
import kotlin.math.min

abstract class AbstractMorphEntity(entityType: EntityType<out AbstractMorphEntity>, world: World
) : MobEntity(entityType, world), PolymerEntity {
    companion object {
        fun createMorphEntityAttributes(): DefaultAttributeContainer.Builder {
            return createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0)
        }
    }

    private var previousHealth: Float = health
    private var previousPlayerHealth: Float? = null

    /**
     * Run every tick while morphed by the morphManager
     */
    fun synchronizeWith(player: ServerPlayerEntity) {
        val thisHealthChanged: Boolean = health != previousHealth
        val playerHealthChanged: Boolean = previousPlayerHealth != null &&
                previousPlayerHealth != player.health

        player.attributes.getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH)?.baseValue =
            this.attributes.getBaseValue(EntityAttributes.GENERIC_MAX_HEALTH)
        this.teleport(player.x, player.y, player.z)
        this.setRotation(player.yaw, player.pitch)
        this.fallDistance = player.fallDistance

        if (thisHealthChanged) {
            // The change in this entity's health takes priority over player health changes
            player.health = this.health
        } else if (playerHealthChanged) {
            this.health = player.health
        }

        this.previousHealth = health
        this.previousPlayerHealth = player.health
    }
}
