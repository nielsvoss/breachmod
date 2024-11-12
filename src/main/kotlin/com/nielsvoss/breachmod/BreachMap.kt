package com.nielsvoss.breachmod

import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import xyz.nucleoid.map_templates.MapTemplate
import xyz.nucleoid.map_templates.MapTemplateSerializer
import xyz.nucleoid.map_templates.TemplateRegion
import xyz.nucleoid.plasmid.game.GameOpenException
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator

class BreachMap private constructor(private val template: MapTemplate, val lobbySpawnRegion: TemplateRegion) {
    companion object {
        @Throws(GameOpenException::class)
        fun load(mapId: Identifier, server: MinecraftServer): BreachMap {
            val template: MapTemplate = MapTemplateSerializer.loadFromResource(server, mapId)
            val lobbySpawnRegion = template.metadata.getFirstRegion("lobbySpawn")
                ?: throw GameOpenException(Text.literal("Provided map did not contain a lobbySpawn region"))
            return BreachMap(template, lobbySpawnRegion)
        }
    }

    fun generator(server: MinecraftServer): TemplateChunkGenerator {
        return TemplateChunkGenerator(server, template)
    }
}