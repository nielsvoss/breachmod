package com.nielsvoss.breachmod.entity

import com.nielsvoss.breachmod.Breach
import eu.pb4.polymer.core.api.entity.PolymerEntity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World

class ExplosiveArrowEntity(entityType : EntityType<out ExplosiveArrowEntity>, world: World, stack : ItemStack, shooter : LivingEntity?)
    : PersistentProjectileEntity(entityType, world, stack), PolymerEntity {
    constructor(entityType : EntityType<out ExplosiveArrowEntity>, world: World)
            : this(entityType, world, ItemStack(Breach.EXPLOSIVE_ARROW), null)
    constructor(world: World) : this(Breach.EXPLOSIVE_ARROW_ENTITY, world)

    override fun getPolymerEntityType(player: ServerPlayerEntity?): EntityType<*> {
        return EntityType.ARROW
    }
}