package com.nielsvoss.breachmod.util

import net.minecraft.util.math.BlockPos

fun BlockPos.toSpaceSeparatedString(): String {
    return "${this.x} ${this.y} ${this.z}"
}
