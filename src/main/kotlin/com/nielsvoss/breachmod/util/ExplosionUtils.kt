package com.nielsvoss.breachmod.util

import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

object ExplosionUtils {
    const val DEFAULT_EXPLOSION_STRENGTH: Float = 4.0F

    @JvmStatic
    fun createExplosion(source: Entity, world: World, targetPos: Vec3d, strength: Float, createFire: Boolean) {
        if (!world.isClient) {
            world.createExplosion(
                source,
                targetPos.x,
                targetPos.y,
                targetPos.z,
                strength,
                createFire,
                World.ExplosionSourceType.TNT
            )
        }
    }
}

