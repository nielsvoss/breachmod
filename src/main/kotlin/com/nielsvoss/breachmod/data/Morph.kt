package com.nielsvoss.breachmod.data

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import xyz.nucleoid.plasmid.util.PlayerRef

class Morph private constructor(player: ServerPlayerEntity) {
    val playerId: PlayerRef = PlayerRef.of(player)
    private val morphData: MorphData = MorphData.loadFrom(player)
    private var hasBeenRestored: Boolean = false

    init {
        MorphData.applyMorphData(player)
    }

    companion object {
        fun create(player: ServerPlayerEntity): Morph {
            return Morph(player)
        }
    }

    fun getMorphedPlayer(world: ServerWorld): ServerPlayerEntity? {
        return playerId.getEntity(world)
    }

    /**
     * Restore the player to their original state
     */
    fun pop(player: ServerPlayerEntity) {
        if (hasBeenRestored) throw IllegalStateException()
        if (player.uuid != playerId.id) throw IllegalArgumentException()
        morphData.restore(player)
        hasBeenRestored = true
    }
}