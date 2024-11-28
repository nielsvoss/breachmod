package com.nielsvoss.breachmod.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.nielsvoss.breachmod.Breach;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(ArrowEntity.class)
abstract class ArrowEntityMixin extends PersistentProjectileEntity {
    @Shadow private Potion potion;

    @Shadow @Final private Set<StatusEffectInstance> effects;

    private ArrowEntityMixin(EntityType<? extends PersistentProjectileEntity> type, World world, ItemStack stack) {
        super(type, world, stack);
    }

    /*
    @WrapMethod(method = "initFromStack")
    public void handleExplosiveArrows(ItemStack stack, Operation<Void> original) {
        if (stack.isOf(Breach.EXPLOSIVE_ARROW)) {
            this.potion = Potions.EMPTY;
            this.effects.clear();
            // this.dataTracker.set(COLOR, -1);
        } else {
            original.call(stack);
        }
    }
     */
}
