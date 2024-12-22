package com.nielsvoss.breachmod.entity

import com.nielsvoss.breachmod.Breach
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
            if (isInBlock && shooter.isSneaking) {
                pull(shooter, projectile)
            }
        }
    }

    override fun getPolymerEntityType(player: ServerPlayerEntity?): EntityType<*> {
        return EntityType.SLIME
    }

    private fun end() {
        // Detaching leash is needed to prevent it from dropping a lead item
        this.detachLeash(true, false)
        this.remove(RemovalReason.DISCARDED)

        val projectile = getProjectile()
        if (projectile != null && (projectile as PersistentProjectileEntityAccessor).inGround) {
            getShooter()?.let { shooter ->
                val v = shooter.velocity
                shooter.velocity = Vec3d(v.x * 1.5, v.y + 0.5, v.z * 1.5)
                shooter.velocityModified = true
            }
        }
    }

    private fun pull(shooter: ServerPlayerEntity, projectile: PersistentProjectileEntity) {
        // Vector from the shooter to the grapple
        val displacement = projectile.pos.subtract(shooter.pos)

        // If v is the velocity, and d is the displacement vector, then this is
        // proj_d(v) = <v, d> / <d, d> * d if <v, d> < 0
        // 0 if <v, d> >= 0
        val opposingVelocityProjected = displacement.multiply(
            shooter.velocity.dotProduct(displacement).coerceAtMost(0.0)
                    / displacement.lengthSquared())

        val maxReelingSpeed = 0.04
        val reelingSpeedPerBlockDisplacement = 0.02
        val reelingSpeed = (displacement.length() * reelingSpeedPerBlockDisplacement).coerceAtMost(maxReelingSpeed)

        val reeling = displacement.normalize().multiply(reelingSpeed)
        val elastic = opposingVelocityProjected.multiply(-0.5)
        val elasticAdjusted = Vec3d(elastic.x, elastic.y * 0.4, elastic.z)

        // Gravity in minecraft is 0.08 blocks/tick^2, this cancels some of that
        val antigravity = Vec3d(0.0, 0.05, 0.0)

        shooter.addVelocity(reeling.add(elasticAdjusted).add(antigravity))

        // Undo horizontal drag. Vanilla minecraft multiplies horizontal movement by 0.91 every tick.
        // This cancels some of that.
        val horizontalAdjustment = 0.91
        // TODO: If player has multiple grapples, this should only apply once per tick
        shooter.velocity = Vec3d(shooter.velocity.x / horizontalAdjustment, shooter.velocity.y, shooter.velocity.z / horizontalAdjustment)

        shooter.velocityModified = true
    }
}
