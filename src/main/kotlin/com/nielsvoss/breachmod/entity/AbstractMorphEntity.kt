package com.nielsvoss.breachmod.entity

import com.nielsvoss.breachmod.data.Morph
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

abstract class AbstractMorphEntity(entityType: EntityType<out AbstractMorphEntity>, world: World
) : MobEntity(entityType, world), PolymerEntity {
    private var morph: Morph? = null

    private fun trySetMorph(morph: Morph?): Boolean {
        if (this.morph != null) {
            val didPop = tryPopMorph()
            if (!didPop) return false
        }
        this.morph = morph
        return true
    }

    private fun tryPopMorph(): Boolean {
        val world = world // Needed to allow smart cast
        val morph = morph
        if (!world.isClient && world is ServerWorld && morph != null) {
            morph.getMorphedPlayer(world)?.let { player ->
                morph.pop(player)
                return true
            }
            // TODO: If player doesn't exist, pop once the player logs back on
        }
        return false
    }

    override fun interactMob(player: PlayerEntity?, hand: Hand?): ActionResult {
        if (!world.isClient && world is ServerWorld && player is ServerPlayerEntity) {
            if (hand == Hand.MAIN_HAND) {
                morph = Morph.create(player)
                val result = trySetMorph(morph)
                if (!result) {
                    player.sendMessage(Text.of("Temporary message (cannot morph because occupied)"))
                }
                return ActionResult.CONSUME
            }
        }
        return super.interactMob(player, hand)
    }

    override fun remove(reason: RemovalReason?) {
        tryPopMorph()
        super.remove(reason)
    }
}