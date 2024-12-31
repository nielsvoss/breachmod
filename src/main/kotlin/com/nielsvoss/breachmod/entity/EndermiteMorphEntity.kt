package com.nielsvoss.breachmod.entity

import net.minecraft.entity.EntityType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World
import xyz.nucleoid.packettweaker.PacketContext

class EndermiteMorphEntity(entityType: EntityType<out EndermiteMorphEntity>,
                           world: World
) : AbstractMorphEntity(entityType, world) {
    override fun getPolymerEntityType(context: PacketContext?): EntityType<*> {
        return EntityType.ENDERMITE
    }
}