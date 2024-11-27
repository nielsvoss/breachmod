package com.nielsvoss.breachmod.util

import net.minecraft.command.argument.EntityAnchorArgumentType
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d

/*
// Not working right now
private fun vector2ToYaw(x: Double, z: Double): Float {
    val radiansCounterclockwiseFromDueSouth = atan2(z, x)
    val radiansClockwiseFromDueSouth = -radiansCounterclockwiseFromDueSouth
    val degreesClockwiseFromDueSouth = radiansClockwiseFromDueSouth * 180.0 / PI
    return degreesClockwiseFromDueSouth.toFloat()
}
 */

fun Entity.teleportFacingOrigin(pos: Vec3d) {
    // this.teleport(world, pos.x, pos.y, pos.z, vector2ToYaw(-this.x, -this.z), 0F)
    this.teleport(pos.x, pos.y, pos.z)
    this.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, Vec3d(0.0, this.eyeY, 0.0))
}
