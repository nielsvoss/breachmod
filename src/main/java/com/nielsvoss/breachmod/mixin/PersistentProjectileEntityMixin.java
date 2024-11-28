package com.nielsvoss.breachmod.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nielsvoss.breachmod.Breach;
import com.nielsvoss.breachmod.BreachRuleTypes;
import com.nielsvoss.breachmod.util.ExplosionUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;

@Mixin(PersistentProjectileEntity.class)
abstract class PersistentProjectileEntityMixin extends ProjectileEntity {
	@Shadow public abstract ItemStack getItemStack();

	private PersistentProjectileEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	@WrapOperation(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private boolean increaseDamageAndHandleExplosions(Entity instance, DamageSource source, float amount, Operation<Boolean> original) {
		boolean isExplosive = this.getItemStack().isOf(Breach.EXPLOSIVE_ARROW);

		var gameSpace = GameSpaceManager.get().byWorld(instance.getWorld());
		if (gameSpace != null && gameSpace.getBehavior().testRule(BreachRuleTypes.ARROWS_INSTANT_KILL) == ActionResult.SUCCESS) {
			if (!isExplosive && this.getType() == EntityType.ARROW) {
				float newDamageAmount = 1_000_000.0F; // Arbitrary large number
				return original.call(instance, source, newDamageAmount);
			}
		}

		boolean originalResult = original.call(instance, source, amount);
		if (isExplosive) {
			// For some reason calling this before the original.call(...) makes the arrow bounce off entities
			ExplosionUtils.createExplosion(this, this.getWorld(), this.getPos(), ExplosionUtils.DEFAULT_EXPLOSION_STRENGTH);
		}
		return originalResult;
	}

	@WrapMethod(method = "onBlockHit")
	public void createExplosions(BlockHitResult blockHitResult, Operation<Void> original) {
		original.call(blockHitResult);
		if (this.getItemStack().isOf(Breach.EXPLOSIVE_ARROW)) {
			ExplosionUtils.createExplosion(this, this.getWorld(), this.getPos(), ExplosionUtils.DEFAULT_EXPLOSION_STRENGTH);
			this.remove(RemovalReason.DISCARDED);
		}
	}
}