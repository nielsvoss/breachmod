package com.nielsvoss.breachmod.entity

import com.nielsvoss.breachmod.Breach
import com.nielsvoss.breachmod.mixin.PersistentProjectileEntityAccessor
import eu.pb4.polymer.core.api.entity.PolymerEntity
import net.minecraft.entity.EntityType
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

        /**
         * Max number of ticks since last sneak in order to initiate a cancel
         */
        private const val CANCEL_TICKS: Int = 10
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
    private var timeSinceLastSneak: Int = -1
    private var grappleLength: Double = Double.NaN

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
                end()
                return
            }

            this.setPosition(projectile.pos)

            if (shooter.isSneaking) {
                if (timeSinceLastSneak in 1..CANCEL_TICKS) {
                    end()
                }
                timeSinceLastSneak = 0
            } else {
                if (timeSinceLastSneak != -1) timeSinceLastSneak++
            }

            // TODO: Once updated to 1.21.4 (which uses datatracker), use isInGround instead
            val isInBlock: Boolean = (projectile as PersistentProjectileEntityAccessor).inGround
            val distanceToGrapple = projectile.pos.subtract(shooter.pos).length()
            if (isInBlock && grappleLength.isNaN()) {
                grappleLength = distanceToGrapple + 1.0
            }

            if (!grappleLength.isNaN()) {
                val maxSpeed = 0.2
                val speedMultiplier = 0.5
                if (shooter.isSneaking) {
                    grappleLength -= 0.2
                }

                if (distanceToGrapple > grappleLength) {
                    val speed = (speedMultiplier * (distanceToGrapple - grappleLength)).coerceAtMost(maxSpeed)
                    val force = projectile.pos.subtract(shooter.pos).normalize().multiply(speed)
                    shooter.addVelocity(force)
                    shooter.velocityModified = true
                }
            }
        }
    }

    override fun getPolymerEntityType(player: ServerPlayerEntity?): EntityType<*> {
        return EntityType.SLIME
    }

    private fun end() {
        // Detaching leash is needed to prevent it from dropping a lead item
        this.detachLeash(true, false)
        this.kill()
    }
}
