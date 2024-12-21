package com.nielsvoss.breachmod;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.jetbrains.annotations.Nullable;

public interface PersistentProjectileEntityDuck {
    @Nullable
    Grapple breach_getGrapple();

    void breach_setGrapple(@Nullable Grapple grapple);

    static Grapple getGrapple(PersistentProjectileEntity entity) {
        return ((PersistentProjectileEntityDuck) entity).breach_getGrapple();
    }

    static void setGrapple(PersistentProjectileEntity entity, @Nullable Grapple grapple) {
        ((PersistentProjectileEntityDuck) entity).breach_setGrapple(grapple);
    }
}
