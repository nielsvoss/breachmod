package com.nielsvoss.breachmod

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.passive.BatEntity
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.world.World
import xyz.nucleoid.plasmid.util.PlayerRef
import java.util.*

class Grapple private constructor(private val shooterId: PlayerRef, private val leashedEntityId: UUID) {
    companion object {
        fun create(world: World, player: ServerPlayerEntity): Grapple {
            val bat: BatEntity = BatEntity(EntityType.BAT, world)
            bat.setPosition(player.pos)
            // bat.isAiDisabled = true
            world.spawnEntity(bat)
            bat.attachLeash(player, true) // TODO: Try other way around, then maybe eliminate bat?
            return Grapple(PlayerRef.of(player), bat.uuid)
        }
    }

    private fun getShooter(world: ServerWorld): ServerPlayerEntity? {
        return shooterId.getEntity(world)
    }

    fun tick(projectile: PersistentProjectileEntity) {
        val world: World = projectile.world
        if (world !is ServerWorld) return

        val shooter = getShooter(world)
        if (shooter != null) {
            // shooter.addVelocity(0.01, 0.0, 0.0)
            // shooter.velocityModified = true
            shooter.sendMessage(Text.of("bleh"))
        }
    }
}
