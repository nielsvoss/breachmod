package com.nielsvoss.breachmod.util

import com.nielsvoss.breachmod.ServerPlayerEntityDuck
import com.nielsvoss.breachmod.data.Morph
import com.nielsvoss.breachmod.entity.AbstractMorphEntity
import net.minecraft.server.network.ServerPlayerEntity
import xyz.nucleoid.plasmid.util.PlayerRef

object MorphManager {
    @JvmStatic
    fun getMorph(player: ServerPlayerEntity): Morph? {
        return (player as ServerPlayerEntityDuck).breach_getMorph()
    }

    /**
     * Returns true if the morph was popped, false otherwise
     */
    @JvmStatic
    fun popMorph(player: ServerPlayerEntity): Boolean {
        val morph: Morph = getMorph(player) ?: return false
        morph.morphData.restore(player)
        (player as ServerPlayerEntityDuck).breach_setMorph(null)
        return true
    }

    @JvmStatic
    fun morphPlayer(player: ServerPlayerEntity, morphedEntity: AbstractMorphEntity) {
        popMorph(player)
        val morph = Morph.create(player, morphedEntity)
        (player as ServerPlayerEntityDuck).breach_setMorph(morph)
        morphedEntity.setAttachedPlayerId(PlayerRef.of(player))
    }
}