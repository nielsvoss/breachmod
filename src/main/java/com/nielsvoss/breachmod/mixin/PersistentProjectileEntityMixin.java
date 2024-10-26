package com.nielsvoss.breachmod.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nielsvoss.breachmod.BreachRuleTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;

@Mixin(PersistentProjectileEntity.class)
abstract class PersistentProjectileEntityMixin extends ProjectileEntity {
	private PersistentProjectileEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	@WrapOperation(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private boolean increaseDamage(Entity instance, DamageSource source, float amount, Operation<Boolean> original) {
		var gameSpace = GameSpaceManager.get().byWorld(instance.getWorld());
		if (gameSpace != null && gameSpace.getBehavior().testRule(BreachRuleTypes.ARROWS_INSTANT_KILL) == ActionResult.SUCCESS) {
			if (this.getType() == EntityType.ARROW) {
				float newDamageAmount = 1_000_000_000.0F; // Arbitrary large number
				return original.call(instance, source, newDamageAmount);
			}
		}
		return original.call(instance, source, amount);
	}
}