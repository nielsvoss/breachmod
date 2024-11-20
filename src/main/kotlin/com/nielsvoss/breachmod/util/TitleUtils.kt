package com.nielsvoss.breachmod.util

import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket
import net.minecraft.network.packet.s2c.play.TitleS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

/**
 * Clears title from player, like /title ... clear
 */
fun ServerPlayerEntity.clearTitle() {
    this.networkHandler.sendPacket(ClearTitleS2CPacket(false))
}

/**
 * Clears title from player and also resets the times back to their default (which according to the wiki is
 * (10 ticks, 70 ticks, 20 ticks)), like /title ... reset
 */
fun ServerPlayerEntity.resetTitle() {
    this.networkHandler.sendPacket(ClearTitleS2CPacket(true))
}

fun ServerPlayerEntity.setTitleTimes(fadeInTicks: Int, stayTicks: Int, fadeOutTicks: Int) {
    this.networkHandler.sendPacket(TitleFadeS2CPacket(fadeInTicks, stayTicks, fadeOutTicks))
}

fun ServerPlayerEntity.sendTitle(text: Text) {
    this.networkHandler.sendPacket(TitleS2CPacket(text))
}

fun ServerPlayerEntity.sendSubtitle(text: Text) {
    this.networkHandler.sendPacket(SubtitleS2CPacket(text))
}
