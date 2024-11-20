package com.nielsvoss.breachmod.util

import net.minecraft.util.math.Vec3d
import xyz.nucleoid.map_templates.BlockBounds

fun BlockBounds.randomBottom(): Vec3d {
    // Adding 0.5 is necessary to convert from the integer coordinates of the block position to the center of the block
    // (rather than the lowest corner of the block)
    val x = lerp(this.min.x.toDouble() + 0.5, this.max.x.toDouble() + 0.5, Math.random())
    val y = this.centerBottom().y
    val z = lerp(this.min.x.toDouble() + 0.5, this.max.x.toDouble() + 0.5, Math.random())
    return Vec3d(x, y, z)
}

private fun lerp(v0: Double, v1: Double, t: Double): Double {
    return (1 - t) * v0 + t * v1
}
