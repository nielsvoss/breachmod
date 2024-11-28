package com.nielsvoss.breachmod.item

import com.nielsvoss.breachmod.Breach
import com.nielsvoss.breachmod.entity.ExplosiveArrowEntity
import eu.pb4.polymer.core.api.item.PolymerItem
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.item.ArrowItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World

class ExplosiveArrowItem(settings: Settings) : ArrowItem(settings), PolymerItem {
    override fun createArrow(world: World, stack: ItemStack, shooter: LivingEntity?): PersistentProjectileEntity {
        //return SpectralArrowEntity(world, shooter, stack)
        return ExplosiveArrowEntity(Breach.EXPLOSIVE_ARROW_ENTITY, world, stack.copyWithCount(1), shooter)
    }

    override fun getPolymerItem(stack: ItemStack, player: ServerPlayerEntity?): Item {
        return Items.TIPPED_ARROW
    }
}
