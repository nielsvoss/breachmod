package com.nielsvoss.breachmod.game

import com.nielsvoss.breachmod.data.BreachMap
import com.nielsvoss.breachmod.state.RoundPersistentState
import com.nielsvoss.breachmod.kit.BreachKit
import com.nielsvoss.breachmod.state.BreachPlayersState
import com.nielsvoss.breachmod.util.*
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.world.GameMode
import xyz.nucleoid.map_templates.TemplateRegion

class BreachActiveSpawnLogic(
    private val world: ServerWorld,
    private val map: BreachMap,
    private val playersState: BreachPlayersState,
    private val persistentState: RoundPersistentState,
    private val availableAttackerKits: List<BreachKit>,
    private val availableDefenderKits: List<BreachKit>,
    private val giveHelmets: Boolean,
) {
    fun spawnPlayers() {
        // To ensure all players spawn in the same region
        val attackersSpawn: TemplateRegion = map.attackerSpawnRegions.random()
        val defendersSpawn: TemplateRegion = map.defenderSpawnRegions.random()
        for (player in playersState.onlineParticipants()) {
            spawnPlayerAtRegion(player, attackersSpawn, defendersSpawn)
        }
    }

    fun spawnPlayer(player: ServerPlayerEntity) {
        spawnPlayerAtRegion(player, map.attackerSpawnRegions.random(), map.defenderSpawnRegions.random())
    }

    /**
     * attackersSpawn and defendersSpawn let you specify a location (if spawning all players at the same location),
     * otherwise it will be chosen at random
     */
    private fun spawnPlayerAtRegion(player: ServerPlayerEntity, attackersSpawn: TemplateRegion, defendersSpawn: TemplateRegion) {
        playersState.markSurviving(player)

        player.inventory.clear()

        var kit: BreachKit? = null
        if (playersState.isAnyAttacker(player)) {
            val loc = attackersSpawn.bounds.randomBottom()
            player.teleportFacingOrigin(world, loc)

            TeamArmorUtils.giveTeamArmor(player, playersState.attackingTeamDyeColor.rgb, giveHelmets)
            kit = persistentState.kitSelections.getAttackerKitOrRandom(player, availableAttackerKits)
        } else if (playersState.isAnyDefender(player)) {

            val loc = defendersSpawn.bounds.randomBottom()
            player.teleportFacingOrigin(world, loc)

            TeamArmorUtils.giveTeamArmor(player, playersState.defendingTeamDyeColor.rgb, giveHelmets)
            kit = persistentState.kitSelections.getDefenderKitOrRandom(player, availableDefenderKits)
        }

        kit?.equipPlayer(player)
        player.changeGameMode(GameMode.SURVIVAL)
        player.health = 20F
        player.hungerManager.foodLevel = 20
        player.fallDistance = 0F
    }

    fun spawnEliminatedPlayer(player: ServerPlayerEntity) {
        val respawnLoc = map.eliminatedSpawnRegions.random().bounds.randomBottom()
        player.health = 20.0F
        player.inventory.clear()
        player.changeGameMode(GameMode.SPECTATOR)
        player.setTitleTimes(0, 20, 5)
        // The title will be occupied with a "x vs y" message.
        player.sendSubtitle(Text.translatable("text.breach.death_title").formatted(Formatting.RED))
        player.teleportFacingOrigin(world, respawnLoc)
    }
}
