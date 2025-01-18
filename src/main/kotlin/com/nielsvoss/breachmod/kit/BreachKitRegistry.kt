package com.nielsvoss.breachmod.kit

import com.google.gson.JsonParser
import com.mojang.serialization.DataResult
import com.mojang.serialization.JsonOps
import com.nielsvoss.breachmod.Breach
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier
import xyz.nucleoid.plasmid.api.util.TinyRegistry
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Originally based on
 * https://github.com/NucleoidMC/skywars/blob/main/src/main/java/us/potatoboy/skywars/kit/KitRegistry.java
 */
object BreachKitRegistry {
    val KITS: TinyRegistry<BreachKit> = TinyRegistry.create()

    fun register() {
        val serverData: ResourceManagerHelper = ResourceManagerHelper.get(ResourceType.SERVER_DATA)

        serverData.registerReloadListener(Identifier.of(Breach.MOD_ID, "breach_kit")) { registries ->
            object : SimpleSynchronousResourceReloadListener {
                override fun getFabricId(): Identifier = Identifier.of(Breach.MOD_ID, "breach_kit")

                override fun reload(manager: ResourceManager) {
                    KITS.clear()
                    val ops = registries.getOps(JsonOps.INSTANCE)

                    manager.findResources("breach_kit") { it.path.endsWith(".json") }.forEach { (path, resource) ->
                        try {
                            BufferedReader(InputStreamReader(resource.inputStream)).use { reader ->
                                val json: com.google.gson.JsonElement? = JsonParser.parseReader(reader)
                                val identifier = identifierFromPath(path)
                                val result: DataResult<BreachKit> = BreachKit.CODEC.decode(ops, json).map { it.first }
                                result.result().ifPresent { kit -> KITS.register(identifier, kit) }
                                result.error().ifPresent { error ->
                                    Breach.LOGGER.error("Failed to decode kit at {}: {}", path, error.toString())
                                }
                            }
                        } catch (e: IOException) {
                            Breach.LOGGER.error("Failed to load kit at {}", path, e)
                        }
                    }
                }
            }
        }
    }

    fun getKits(identifiers: List<Identifier>, categories: List<String>): List<BreachKit> {
        val kits: MutableList<BreachKit> = mutableListOf()
        for (id in identifiers) {
            val kit: BreachKit? = KITS.get(id)
            if (kit != null) {
               kits.add(kit)
            } else {
                Breach.LOGGER.error("Kit {} not found", id)
            }
        }

        for (category in categories) {
            KITS.values().filter { kit -> category in kit.categories }
                .filter { it !in kits }
                .sortedBy { it.nameTranslationKey }
                .forEach { kits.add(it) }
        }

        return kits
    }
}

/**
 * Converts modid:breach_kit/foobar.json into modid:foobar
 */
private fun identifierFromPath(location: Identifier): Identifier {
    val path = location.path.substring("breach_kit/".length, location.path.length - ".json".length)
    return Identifier.of(location.namespace, path)
}
