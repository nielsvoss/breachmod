package com.nielsvoss.breachmod.mixin;

import com.mojang.authlib.GameProfile;
import com.nielsvoss.breachmod.ServerPlayerEntityDuck;
import com.nielsvoss.breachmod.data.Morph;
import com.nielsvoss.breachmod.entity.AbstractMorphEntity;
import com.nielsvoss.breachmod.util.MorphManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.core.jmx.Server;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerEntityDuck {
    @Shadow public abstract ServerWorld getServerWorld();

    private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Unique
    private @Nullable Morph morph = null;

    @Override
    public @Nullable Morph breach_getMorph() {
        return morph;
    }

    @Override
    public void breach_setMorph(@Nullable Morph morph) {
        this.morph = morph;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void updateMorph(CallbackInfo ci) {
        Morph morph = breach_getMorph();
        if (morph != null) {
            AbstractMorphEntity morphedEntity = morph.getMorphedEntity(this.getServerWorld());
            if (morphedEntity == null || morphedEntity.isDead() || morphedEntity.getHealth() <= 0.0001) {
                MorphManager.popMorph((ServerPlayerEntity) (Object) this);
            }
        }
    }
}
