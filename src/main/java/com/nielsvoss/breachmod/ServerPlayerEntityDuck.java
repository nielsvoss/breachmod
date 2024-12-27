package com.nielsvoss.breachmod;

import com.nielsvoss.breachmod.data.Morph;
import org.jetbrains.annotations.Nullable;

public interface ServerPlayerEntityDuck {
    @Nullable
    Morph breach_getMorph();
    void breach_setMorph(@Nullable Morph morph);
}
