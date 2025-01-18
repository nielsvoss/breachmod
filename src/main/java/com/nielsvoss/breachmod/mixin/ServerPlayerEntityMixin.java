package com.nielsvoss.breachmod.mixin;

import com.mojang.authlib.GameProfile;
import com.nielsvoss.breachmod.ServerPlayerEntityDuck;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerEntityDuck {
    private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Unique
    private int bowRightClickTimer = 0;

    @Unique
    private boolean wasGrappleActiveSinceLastTouchingGround = false;

    @Override
    public void breach_setWasGrappleActiveSinceLastTouchingGround(boolean b) {
        this.wasGrappleActiveSinceLastTouchingGround = b;
    }

    @Override
    public boolean breach_rightClickedWithBowRecently() {
        return this.bowRightClickTimer > 0;
    }

    @Override
    public void breach_setJustRightClickedWithBow() {
        this.bowRightClickTimer = 2;
    }

    /*
    // This worked in 1.20.4
    @Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;increaseTravelMotionStats(DDD)V"))
    public void removeDrag(Vec3d movementInput, CallbackInfo ci) {
        if (!this.getWorld().isClient()) {
            if (!this.isOnGround() && wasGrappleActiveSinceLastTouchingGround) {
                // Re-create the code that is run on the client to determine if the player experiences drag.
                // Each boolean below corresponds to the condition of an if statement, and only if all four of them are
                // false does the statement
                // this.setVelocity(vec3d6.x * (double)f, q * 0.9800000190734863, vec3d6.z * (double)f);
                // end up running.
                FluidState fluidState = this.getWorld().getFluidState(this.getBlockPos());
                boolean b1 = this.isTouchingWater() && this.shouldSwimInFluids() && !this.canWalkOnFluid(fluidState);
                boolean b2 = this.isInLava() && this.shouldSwimInFluids() && !this.canWalkOnFluid(fluidState);
                boolean b3 = this.isFallFlying();
                boolean b4 = this.hasNoDrag();

                if (!b1 && !b2 && !b3 && !b4) {
                    double horizontalDragConstant = 0.91;
                    double verticalDragConstant = 0.9800000190734863;
                    Vec3d v = this.getVelocity();
                    this.setVelocity(v.x / horizontalDragConstant, v.y / verticalDragConstant, v.z / horizontalDragConstant);
                    this.velocityModified = true;
                }
            }
        }
    }
     */

    @Inject(method = "tick", at = @At("RETURN"))
    public void endOfTick(CallbackInfo ci) {
        if (wasGrappleActiveSinceLastTouchingGround && this.isOnGround()) {
            this.wasGrappleActiveSinceLastTouchingGround = false;
        }

        if (wasGrappleActiveSinceLastTouchingGround) {
            // Recreate conditions under which LivingEntity#travelMidAir is called in LivingEntity#travel
            // We need to do this here instead of hooking into LivingEntity#travel because players update movement on
            // the client side and this needs to run on the server side.

            FluidState fluidState = this.getWorld().getFluidState(this.getBlockPos());
            boolean isInFluid = (this.isTouchingWater() || this.isInLava())
                    && this.shouldSwimInFluids() && !this.canWalkOnFluid(fluidState);
            if (!isInFluid && !this.isGliding()) {
                double horizontalDragConstant = 0.91;
                double verticalDragConstant = 0.9800000190734863;
                Vec3d v = this.getVelocity();
                this.setVelocity(v.x / horizontalDragConstant, v.y / verticalDragConstant, v.z / horizontalDragConstant);
                this.velocityModified = true;
            }
        }

        if (bowRightClickTimer > 0) bowRightClickTimer--;
    }
}
