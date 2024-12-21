package com.nielsvoss.breachmod;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.jetbrains.annotations.Nullable;

public interface PersistentProjectileEntityDuck {
    @Nullable
    GrappleEntity breach_getGrapple();

    void breach_setGrapple(@Nullable GrappleEntity grapple);

    static GrappleEntity getGrapple(PersistentProjectileEntity entity) {
        return ((PersistentProjectileEntityDuck) entity).breach_getGrapple();
    }

    static void setGrapple(PersistentProjectileEntity entity, @Nullable GrappleEntity grapple) {
        ((PersistentProjectileEntityDuck) entity).breach_setGrapple(grapple);
    }
}
