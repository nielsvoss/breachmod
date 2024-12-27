package com.nielsvoss.breachmod.entity

import com.nielsvoss.breachmod.ServerPlayerEntityDuck
import com.nielsvoss.breachmod.data.Morph
import com.nielsvoss.breachmod.util.MorphManager
import eu.pb4.polymer.core.api.entity.PolymerEntity
import net.minecraft.entity.EntityType
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.world.World
import xyz.nucleoid.plasmid.util.PlayerRef

abstract class AbstractMorphEntity(entityType: EntityType<out AbstractMorphEntity>, world: World
) : MobEntity(entityType, world), PolymerEntity {
    /**
     * This is supposed to act as a weak reference to the player, which should be broken automatically upon next use
     * when the player is not morphed into this anymore.
     */
    private var attachedPlayerId: PlayerRef? = null

    private fun getAttachedPlayer(): ServerPlayerEntity? {
        val world = world
        if (world.isClient || world !is ServerWorld) return null
        val player: ServerPlayerEntity = attachedPlayerId?.getEntity(world) ?: return null
        val morph: Morph? = (player as ServerPlayerEntityDuck).breach_getMorph()
        if (morph == null || morph.morphEntityId != this.uuid) {
            this.attachedPlayerId = null
            return null
        } else {
            return player
        }
    }

    fun setAttachedPlayerId(id: PlayerRef?) {
        this.attachedPlayerId = id
    }

    /*
    // Moved into getAttachedPlayer()
    override fun tick() {
        super.tick()
        val attachedPlayer = getAttachedPlayer()
        if (attachedPlayer != null) {
            val morph: Morph? = (attachedPlayer as ServerPlayerEntityDuck).breach_getMorph()
            if (morph == null || morph.morphEntityId != this.uuid) {
                attachedPlayerId = null
            }
        }
    }
     */

    override fun interactMob(player: PlayerEntity?, hand: Hand?): ActionResult {
        if (!world.isClient && world is ServerWorld && player is ServerPlayerEntity) {
            if (hand == Hand.MAIN_HAND) {
                if (getAttachedPlayer() == null) {
                    MorphManager.morphPlayer(player, this)
                } else {
                    player.sendMessage(Text.translatable("text.breach.morph_in_use"))
                }
                return ActionResult.CONSUME
            }
        }
        return super.interactMob(player, hand)
    }
}