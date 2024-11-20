package com.nielsvoss.breachmod.util

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text

// out PlayerEntity (or ? extends PlayerEntity in Java) not needed because we are using Collection, not something like MutableList
fun Collection<PlayerEntity>.broadcast(text: Text) {
    this.forEach { it.sendMessage(text) }
}
