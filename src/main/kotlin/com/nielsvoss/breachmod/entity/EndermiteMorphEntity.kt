package com.nielsvoss.breachmod.entity

import net.minecraft.entity.EntityType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World

class EndermiteMorphEntity(entityType: EntityType<out EndermiteMorphEntity>,
                           world: World
) : AbstractMorphEntity(entityType, world) {
    override fun getPolymerEntityType(player: ServerPlayerEntity?): EntityType<*> {
        return EntityType.ENDERMITE
    }
}