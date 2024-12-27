package com.nielsvoss.breachmod.state

import com.nielsvoss.breachmod.data.Morph
import com.nielsvoss.breachmod.entity.AbstractMorphEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import xyz.nucleoid.plasmid.util.PlayerRef

class MorphManager {
    private val morphs: MutableMap<PlayerRef, Morph> = mutableMapOf()

    fun getMorph(player: ServerPlayerEntity): Morph? {
        return morphs[PlayerRef.of(player)]
    }

    /**
     * Returns true if the morph was popped, false otherwise
     */
    fun popMorph(player: ServerPlayerEntity): Boolean {
        val morph: Morph = getMorph(player) ?: return false
        morph.morphData.restore(player)
        morphs.remove(PlayerRef.of(player))
        return true
    }

    fun morphPlayer(player: ServerPlayerEntity, morphedEntity: AbstractMorphEntity) {
        popMorph(player)
        val morph = Morph.create(player, morphedEntity)
        morphs[PlayerRef.of(player)] = morph
        // morphedEntity.setAttachedPlayerId(PlayerRef.of(player))
    }

    fun tick(world: ServerWorld) {
        for ((playerId, morph) in morphs.entries) {
            val morphedEntity: AbstractMorphEntity? = morph.getMorphedEntity(world)
            val player: ServerPlayerEntity? = playerId.getEntity(world)
            if (player != null) {
                if (morphedEntity == null || morphedEntity.isDead || morphedEntity.health <= 0.0001) {
                    popMorph(player)
                } else {
                    morphedEntity.synchronizeWith(player)
                }
            }
        }
    }
}