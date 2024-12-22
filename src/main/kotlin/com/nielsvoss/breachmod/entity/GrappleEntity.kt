package com.nielsvoss.breachmod.entity

import com.nielsvoss.breachmod.Breach
import com.nielsvoss.breachmod.mixin.PersistentProjectileEntityAccessor
import eu.pb4.polymer.core.api.entity.PolymerEntity
import net.minecraft.entity.EntityType
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.world.World
import xyz.nucleoid.plasmid.util.PlayerRef
import java.util.*

class GrappleEntity(entityType: EntityType<out GrappleEntity>, world: World)
    : MobEntity(entityType, world), PolymerEntity {
    companion object {
        fun create(world: ServerWorld, projectile: PersistentProjectileEntity, player: ServerPlayerEntity): GrappleEntity {
            val grapple: GrappleEntity = GrappleEntity(Breach.GRAPPLE_ENTITY_TYPE, world)
            grapple.setPosition(projectile.pos)
            world.spawnEntity(grapple)
            grapple.attachLeash(player, true)
            grapple.shooterId = PlayerRef.of(player)
            grapple.projectileId = projectile.uuid
            return grapple
        }
    }

    init {
        isAiDisabled = true
        isInvisible = true
        isInvulnerable = true
        isSilent = true
        setNoGravity(true)
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
        if (!world.isClient && world is ServerWorld) {
            val shooter: ServerPlayerEntity? = getShooter()
            val projectile: PersistentProjectileEntity? = getProjectile()
            if (shooter == null || projectile == null || shooter.isDead) {
                // Detaching leash is needed to prevent it from dropping a lead item
                this.detachLeash(true, false)
                this.kill()
                return
            }

            this.setPosition(projectile.pos)
            val isInBlock: Boolean = (projectile as PersistentProjectileEntityAccessor).inGround
            if (isInBlock && shooter.isSneaking) {
                shooter.addVelocity((projectile.pos.subtract(shooter.pos)).normalize().multiply(0.1))
                shooter.velocityModified = true
            }
        }
    }

    override fun getPolymerEntityType(player: ServerPlayerEntity?): EntityType<*> {
        return EntityType.SLIME
    }
}
