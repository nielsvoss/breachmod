package com.nielsvoss.breachmod.entity

import net.minecraft.entity.EntityType
import net.minecraft.world.World
import xyz.nucleoid.packettweaker.PacketContext

class SilverfishMorphEntity(entityType: EntityType<out SilverfishMorphEntity>,
                            world: World
) : AbstractMorphEntity(entityType, world) {
    override fun getPolymerEntityType(context: PacketContext?): EntityType<*> {
        return EntityType.SILVERFISH
    }
}