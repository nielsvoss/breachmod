package com.nielsvoss.breachmod.util

import net.minecraft.screen.ScreenHandlerType

// Based on code in
// https://github.com/NucleoidMC/skywars/blob/1.20/src/main/java/us/potatoboy/skywars/game/ui/KitSelectorUI.java
private fun getType(size: Int): ScreenHandlerType<*> {
    return if (size <= 8) {
        ScreenHandlerType.GENERIC_9X1
    } else if (size <= 17) {
        ScreenHandlerType.GENERIC_9X2
    } else if (size <= 26) {
        ScreenHandlerType.GENERIC_9X3
    } else if (size <= 35) {
        ScreenHandlerType.GENERIC_9X4
    } else if (size <= 44) {
        ScreenHandlerType.GENERIC_9X5
    } else {
        ScreenHandlerType.GENERIC_9X6
    }
}

object UIUtils {
    fun getTypeOrThrowIfTooLarge(size: Int): ScreenHandlerType<*> {
        if (size > 53) throw IllegalArgumentException("Too many entries to be selectable in the UI")
        else return getType(size)
    }
}
