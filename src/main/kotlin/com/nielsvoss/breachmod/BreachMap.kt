package com.nielsvoss.breachmod

import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import xyz.nucleoid.map_templates.MapTemplate
import xyz.nucleoid.map_templates.MapTemplateSerializer
import xyz.nucleoid.map_templates.TemplateRegion
import xyz.nucleoid.plasmid.game.GameOpenException
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator

class BreachMap private constructor(private val template: MapTemplate, val lobbySpawnRegion: TemplateRegion,
                                    val targets: List<BreachTarget>) {
    companion object {
        @Throws(GameOpenException::class)
        fun load(mapId: Identifier, server: MinecraftServer): BreachMap {
            val template: MapTemplate = MapTemplateSerializer.loadFromResource(server, mapId)
            val lobbySpawnRegion = template.metadata.getFirstRegion("lobbySpawn")
                ?: throw GameOpenException(Text.of("Provided map did not contain a lobbySpawn region"))

            val targetBlocks = mutableListOf<BlockPos>()
            for (region in template.metadata.getRegions("target")) {
                if (region.bounds.min != region.bounds.max) {
                    throw GameOpenException(Text.of("A target region is not 1x1x1"))
                }
                targetBlocks.add(region.bounds.min)
            }
            val targets: List<BreachTarget> = targetBlocks.map { BreachTarget(it, template.getBlockState(it).block) }
            return BreachMap(template, lobbySpawnRegion, targets)
        }
    }

    fun generator(server: MinecraftServer): TemplateChunkGenerator {
        return TemplateChunkGenerator(server, template)
    }
}