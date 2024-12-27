package com.nielsvoss.breachmod.entity

import eu.pb4.polymer.core.api.entity.PolymerEntity
import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.MobEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World

abstract class AbstractMorphEntity(entityType: EntityType<out AbstractMorphEntity>, world: World
) : MobEntity(entityType, world), PolymerEntity {
    companion object {
        fun createMorphEntityAttributes(): DefaultAttributeContainer.Builder {
            return createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0)
        }
    }

    /**
     * Run every tick while morphed by the morphManager
     */
    fun synchronizeWith(player: ServerPlayerEntity) {
        player.attributes.getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH)?.baseValue =
            this.attributes.getBaseValue(EntityAttributes.GENERIC_MAX_HEALTH)
        player.health = this.health
        this.teleport(player.x, player.y, player.z)
    }
}
