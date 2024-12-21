package com.nielsvoss.breachmod

import net.minecraft.entity.EntityType
import net.minecraft.entity.passive.BatEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World
import xyz.nucleoid.plasmid.util.PlayerRef
import java.util.*

class Grapple private constructor(val playerId: PlayerRef, val leashedEntityId: UUID) {
    companion object {
        fun create(world: World, player: ServerPlayerEntity): Grapple {
            val bat: BatEntity = BatEntity(EntityType.BAT, world)
            bat.isAiDisabled = true
            world.spawnEntity(bat)
            return Grapple(PlayerRef.of(player), bat.uuid)
        }
    }
}
