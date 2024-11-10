package com.nielsvoss.breachmod

import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import xyz.nucleoid.map_templates.MapTemplate
import xyz.nucleoid.map_templates.MapTemplateSerializer
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator

class BreachMap private constructor(private val mapId : Identifier) {
    companion object {
        fun load(mapId: Identifier): BreachMap {
            return BreachMap(mapId)
        }
    }

    fun generator(server: MinecraftServer): TemplateChunkGenerator {
        val template: MapTemplate = MapTemplateSerializer.loadFromResource(server, this.mapId)
        return TemplateChunkGenerator(server, template)
    }
}