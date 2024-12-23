package com.nielsvoss.breachmod.data

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
                                    val lobbyToRemoveRegion: TemplateRegion?, val targets: List<BreachTarget>,
                                    val attackerSpawnRegions: List<TemplateRegion>,
                                    val defenderSpawnRegions: List<TemplateRegion>,
                                    val eliminatedSpawnRegions: List<TemplateRegion>) {
    companion object {
        @Throws(GameOpenException::class)
        fun load(mapId: Identifier, server: MinecraftServer): BreachMap {
            val template: MapTemplate = MapTemplateSerializer.loadFromResource(server, mapId)
            val lobbySpawnRegion = template.metadata.getFirstRegion("lobby_spawn")
                ?: throw GameOpenException(Text.of("Provided map did not contain a lobby_spawn region"))
            val lobbyToRemoveRegion = template.metadata.getFirstRegion("lobby_to_remove")

            val targetBlocks = mutableListOf<BlockPos>()
            for (region in template.metadata.getRegions("target")) {
                if (region.bounds.min != region.bounds.max) {
                    throw GameOpenException(Text.of("A target region is not 1x1x1"))
                }
                targetBlocks.add(region.bounds.min)
            }
            val targets: List<BreachTarget> = targetBlocks.map { BreachTarget(it, template.getBlockState(it).block) }

            val attackerSpawnRegions: List<TemplateRegion> = template.metadata.getRegions("attacker_spawn").toList()
            val defenderSpawnRegions: List<TemplateRegion> = template.metadata.getRegions("defender_spawn").toList()
            val eliminatedSpawnRegions: List<TemplateRegion> = template.metadata.getRegions("eliminated_spawn").toList()
            if (attackerSpawnRegions.isEmpty()) {
                throw GameOpenException(Text.of("Map did not contain any attacking team spawn regions"))
            }
            if (defenderSpawnRegions.isEmpty()) {
                throw GameOpenException(Text.of("Map did not contain any defending team spawn regions"))
            }
            if (eliminatedSpawnRegions.isEmpty()) {
                throw GameOpenException(Text.of("Map did not contain any eliminated spawn regions"))
            }
            return BreachMap(template, lobbySpawnRegion, lobbyToRemoveRegion, targets, attackerSpawnRegions,
                defenderSpawnRegions, eliminatedSpawnRegions)
        }
    }

    fun generator(server: MinecraftServer): TemplateChunkGenerator {
        return TemplateChunkGenerator(server, template)
    }
}