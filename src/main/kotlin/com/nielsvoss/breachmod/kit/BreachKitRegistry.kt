package com.nielsvoss.breachmod.kit

import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.resource.ResourceType
import xyz.nucleoid.plasmid.api.util.TinyRegistry

/**
 * Originally based on
 * https://github.com/NucleoidMC/skywars/blob/main/src/main/java/us/potatoboy/skywars/kit/KitRegistry.java
 */
object BreachKitRegistry {
    val KITS: TinyRegistry<BreachKit> = TinyRegistry.create()

    fun register() {
        val serverData: ResourceManagerHelper = ResourceManagerHelper.get(ResourceType.SERVER_DATA)
        TODO()
    }
}