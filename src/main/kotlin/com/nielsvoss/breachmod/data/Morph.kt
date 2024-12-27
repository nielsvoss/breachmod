package com.nielsvoss.breachmod.data

import com.nielsvoss.breachmod.Breach
import com.nielsvoss.breachmod.ServerPlayerEntityDuck
import com.nielsvoss.breachmod.entity.AbstractMorphEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import xyz.nucleoid.plasmid.util.PlayerRef
import java.util.UUID

class Morph private constructor(player: ServerPlayerEntity) {
    // TODO: Consider whether storing player UUID is really necessary
    val playerId: PlayerRef = PlayerRef.of(player)
    var morphEntityId: UUID? = null
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

    fun getMorphedEntity(world: ServerWorld): AbstractMorphEntity? {
        return this.morphEntityId?.let { world.getEntity(it) } as? AbstractMorphEntity
    }

    // TODO: Maybe this isn't something that should be invoked on a Morph and a player, it should just take a player
    /**
     * Restore the player to their original state
     */
    fun pop(player: ServerPlayerEntity) {
        if (hasBeenRestored) throw IllegalStateException()
        if (player.uuid != playerId.id) Breach.LOGGER.warn("Player's UUID did not match UUID of morph being popped")
        if ((player as ServerPlayerEntityDuck).breach_getMorph() == this) {
            player.breach_setMorph(null)
        } else {
            Breach.LOGGER.warn("Player's morph did not match morph being popped")
        }
        morphData.restore(player)
        hasBeenRestored = true
    }
}