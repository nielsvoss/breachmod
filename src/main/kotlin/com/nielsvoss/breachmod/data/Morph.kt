package com.nielsvoss.breachmod.data

import com.nielsvoss.breachmod.Breach
import com.nielsvoss.breachmod.entity.AbstractMorphEntity
import net.minecraft.entity.Entity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import java.util.*

data class Morph(val morphEntityId: UUID, val morphData: MorphData) {
    companion object {
        fun create(player: ServerPlayerEntity, morphEntity: AbstractMorphEntity): Morph {
            val morph = Morph(morphEntity.uuid, MorphData.loadFrom(player))
            MorphData.applyMorphData(player, morphEntity)
            return morph
        }
    }

    fun getMorphedEntity(world: ServerWorld): AbstractMorphEntity? {
        val entity: Entity = world.getEntity(this.morphEntityId) ?: return null
        if (entity !is AbstractMorphEntity) {
            Breach.LOGGER.warn("Morph had a UUID that wasn't an AbstractMorphEntity")
            return null
        }
        return entity
    }
}
