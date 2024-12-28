package com.nielsvoss.breachmod.entity

import com.nielsvoss.breachmod.Breach
import com.nielsvoss.breachmod.ServerPlayerEntityDuck
import com.nielsvoss.breachmod.mixin.PersistentProjectileEntityAccessor
import eu.pb4.polymer.core.api.entity.PolymerEntity
import net.minecraft.entity.EntityType
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import xyz.nucleoid.plasmid.util.PlayerRef
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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
    private var hasBeenEnded: Boolean = false

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
            if (shooter == null || projectile == null || shooter.isDead || !this.isLeashed) {
                end(false)
                return
            } else if (this.age > 5 && (shooter as ServerPlayerEntityDuck).breach_rightClickedWithBowRecently()) {
                end (true)
                return
            }

            this.setPosition(projectile.pos)

            // TODO: Once updated to 1.21.4 (which uses datatracker), use isInGround instead
            val isInBlock: Boolean = (projectile as PersistentProjectileEntityAccessor).inGround
            if (isInBlock) {
                pull(shooter, projectile, shooter.isSneaking)
            }
        }
    }

    override fun getPolymerEntityType(player: ServerPlayerEntity?): EntityType<*> {
        return EntityType.SLIME
    }

    private fun end(manual: Boolean) {
        if (!hasBeenEnded) {
            // Detaching leash is needed to prevent it from dropping a lead item
            this.detachLeash(true, false)
            this.remove(RemovalReason.DISCARDED)

            if (manual) {
                val projectile = getProjectile()
                if (projectile != null && (projectile as PersistentProjectileEntityAccessor).inGround) {
                    getShooter()?.let { shooter ->
                        if (!shooter.isOnGround) {
                            val v = shooter.rotationVector.normalize().multiply(0.2, 0.7, 0.2)
                            shooter.addVelocity(v)
                            shooter.velocityModified = true
                        }
                    }
                }
            }
            hasBeenEnded = true
        }
    }

    private fun pull(shooter: ServerPlayerEntity, projectile: PersistentProjectileEntity, isActive: Boolean) {
        // Vector from the shooter to the grapple
        val displacement = projectile.pos.subtract(shooter.pos)

        // If v is the velocity, and d is the displacement vector, then this is
        // proj_d(v) = <v, d> / <d, d> * d if <v, d> < 0
        // 0 if <v, d> >= 0
        val opposingVelocityProjected = displacement.multiply(
            shooter.velocity.dotProduct(displacement).coerceAtMost(0.0)
                    / displacement.lengthSquared())

        val elastic = opposingVelocityProjected.multiply(-0.7)
        shooter.addVelocity(elastic)

        if (isActive) {
            val maxVReelingVelocity = 0.4
            val vReelingAcceleration = max(0.04, abs(displacement.y) * 0.06)

            val maxHReelingVelocity = 0.8
            val hReelingAcceleration = max(0.01, displacement.horizontalLength() * 0.0025)

            val reeling = displacement.normalize().multiply(hReelingAcceleration, vReelingAcceleration, hReelingAcceleration)

            val v = shooter.velocity
            val newVx = increaseCapped(v.x, reeling.x, maxHReelingVelocity)
            val newVy = increaseCapped(v.y, reeling.y, maxVReelingVelocity)
            val newVz = increaseCapped(v.z, reeling.z, maxHReelingVelocity)
            shooter.velocity = Vec3d(newVx, newVy, newVz)

            // val look = shooter.rotationVector.normalize().multiply(0.1)
            // shooter.addVelocity(look)

            // Gravity in minecraft is 0.08 blocks/tick^2, this cancels some of that
            // val antigravity = Vec3d(0.0, 0.05, 0.0)
            // shooter.addVelocity(antigravity)
        }

        // Undo horizontal drag. Vanilla minecraft multiplies horizontal movement by 0.91 every tick.
        if (!shooter.isOnGround) {
            (shooter as ServerPlayerEntityDuck).breach_setWasGrappleActiveSinceLastTouchingGround(true);
        }

        shooter.velocityModified = true
    }
}

/**
 * Increases current by increase, but won't increase it beyond maxAbsIfIncrease, and won't increase its absolute value
 * it if it already has an absolute value >= maxAbsIfIncrease.
 */
private fun increaseCapped(current: Double, increase: Double, maxAbsIfIncrease: Double): Double =
    if (current > maxAbsIfIncrease) min(current, current + increase)
    else if (current < -maxAbsIfIncrease) max(current, current + increase)
    else (current + increase).coerceIn(-maxAbsIfIncrease, maxAbsIfIncrease)
