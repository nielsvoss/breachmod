package com.nielsvoss.breachmod.entity

import eu.pb4.polymer.core.api.entity.PolymerEntity
import net.minecraft.entity.EntityType
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.mob.MobEntity
import net.minecraft.world.World

abstract class AbstractMorphEntity(entityType: EntityType<out AbstractMorphEntity>, world: World
) : MobEntity(entityType, world), PolymerEntity {
    companion object {
        fun createMorphEntityAttributes(): DefaultAttributeContainer.Builder {
            return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0)
        }
    }
}
