package com.nielsvoss.breachmod;

public interface ServerPlayerEntityDuck {
    void breach_setWasGrappleActiveSinceLastTouchingGround(boolean b);
    boolean breach_rightClickedWithBowRecently();
    void breach_setJustRightClickedWithBow();
}
