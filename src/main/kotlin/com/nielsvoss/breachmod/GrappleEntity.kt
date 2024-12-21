package com.nielsvoss.breachmod

import eu.pb4.polymer.core.api.entity.PolymerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.passive.BatEntity
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.world.World
import xyz.nucleoid.plasmid.util.PlayerRef
import java.util.*

class GrappleEntity(entityType: EntityType<out GrappleEntity>, world: World)
    : MobEntity(entityType, world), PolymerEntity {
    companion object {
        fun create(world: ServerWorld, projectile: PersistentProjectileEntity, player: ServerPlayerEntity): GrappleEntity {
            val grapple: GrappleEntity = GrappleEntity(Breach.GRAPPLE_ENTITY_TYPE, world)
            grapple.setPosition(player.pos)
            grapple.isAiDisabled = true
            world.spawnEntity(grapple)
            grapple.attachLeash(player, true)
            grapple.shooterId = PlayerRef.of(player)
            grapple.projectileId = projectile.uuid
            return grapple
        }
    }

    private var shooterId: PlayerRef? = null
    private var projectileId: UUID? = null

    private fun getShooter(): ServerPlayerEntity? {
        val world = this.world
        return if (!world.isClient && world is ServerWorld) {
            shooterId?.getEntity(world)
        } else {
            null
        }
    }

    private fun getProjectile(): PersistentProjectileEntity? {
        val world = this.world
        return if (!world.isClient && world is ServerWorld) {
            this.projectileId?.let { world.getEntity(it) } as? PersistentProjectileEntity
        } else {
            null
        }
    }

    override fun tick() {
        super.tick()
        println(this.age)
    }

    override fun getPolymerEntityType(player: ServerPlayerEntity?): EntityType<*> {
        return EntityType.BAT
    }
}
