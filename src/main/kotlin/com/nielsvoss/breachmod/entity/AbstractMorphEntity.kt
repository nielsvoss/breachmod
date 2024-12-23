package com.nielsvoss.breachmod.entity

import com.nielsvoss.breachmod.data.Morph
import eu.pb4.polymer.core.api.entity.PolymerEntity
import net.minecraft.entity.EntityType
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.world.World

abstract class AbstractMorphEntity(entityType: EntityType<out AbstractMorphEntity>, world: World
) : MobEntity(entityType, world), PolymerEntity {
    override fun interactMob(player: PlayerEntity?, hand: Hand?): ActionResult {
        if (!world.isClient && world is ServerWorld && player is ServerPlayerEntity) {
            if (hand == Hand.MAIN_HAND) {
                println("bleh")
                Morph.create(player)
                return ActionResult.CONSUME
            }
        }
        return super.interactMob(player, hand)
    }
}