package com.nielsvoss.breachmod.game

import com.nielsvoss.breachmod.Breach
import com.nielsvoss.breachmod.BreachRuleTypes
import com.nielsvoss.breachmod.BreachStatistics
import com.nielsvoss.breachmod.config.BreachGameConfig
import com.nielsvoss.breachmod.data.BreachMap
import com.nielsvoss.breachmod.data.BreachTarget
import com.nielsvoss.breachmod.entity.AbstractMorphEntity
import com.nielsvoss.breachmod.state.*
import com.nielsvoss.breachmod.ui.SpawnSelectorUI
import com.nielsvoss.breachmod.ui.TargetSelectorUI
import com.nielsvoss.breachmod.util.*
import eu.pb4.sidebars.api.Sidebar
import net.minecraft.block.Blocks
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import xyz.nucleoid.map_templates.TemplateRegion
import xyz.nucleoid.plasmid.api.game.GameOpenException
import xyz.nucleoid.plasmid.api.game.GameSpace
import xyz.nucleoid.plasmid.api.game.event.GameActivityEvents
import xyz.nucleoid.plasmid.api.game.rule.GameRuleType
import xyz.nucleoid.plasmid.api.game.stats.GameStatisticBundle
import xyz.nucleoid.plasmid.api.game.stats.StatisticKeys
import xyz.nucleoid.plasmid.api.util.PlayerRef
import xyz.nucleoid.stimuli.event.EventResult
import xyz.nucleoid.stimuli.event.block.BlockBreakEvent
import xyz.nucleoid.stimuli.event.player.PlayerAttackEntityEvent
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent

// Designed similarly to https://github.com/NucleoidMC/skywars/blob/1.20/src/main/java/us/potatoboy/skywars/game/SkyWarsActive.java
class BreachActive private constructor(private val gameSpace: GameSpace, private val world: ServerWorld,
                                       private val map: BreachMap, private val config: BreachGameConfig,
                                       private val persistentState: RoundPersistentState,
                                       private val players: BreachPlayersState) {
    companion object {
        @Throws(GameOpenException::class)
        fun open(gameSpace: GameSpace, world: ServerWorld, map: BreachMap, config: BreachGameConfig,
                 persistentState: RoundPersistentState
        ) {
            gameSpace.setActivity { activity ->
                val breachPlayersState: BreachPlayersState = BreachPlayersState.create(
                    activity, persistentState.teamState, world.players
                )
                val breachActive = BreachActive(gameSpace, world, map, config, persistentState, breachPlayersState)

                if (config.gameplayOptions.arrowsInstantKill) {
                    activity.allow(BreachRuleTypes.ARROWS_INSTANT_KILL)
                }
                if (config.gameplayOptions.disableHunger) {
                    activity.deny(GameRuleType.HUNGER)
                    // Slows down natural regeneration, but does not stop it
                    activity.deny(GameRuleType.SATURATED_REGENERATION)
                }
                if (config.gameplayOptions.disableTileDrops) {
                    activity.deny(GameRuleType.BLOCK_DROPS)
                }
                if (config.gameplayOptions.disableFireTick) {
                    activity.deny(GameRuleType.FIRE_TICK)
                }

                activity.deny(GameRuleType.PORTALS)
                activity.deny(GameRuleType.MODIFY_ARMOR)

                activity.listen(GameActivityEvents.TICK, GameActivityEvents.Tick { breachActive.tick() })
                activity.listen(PlayerDeathEvent.EVENT, PlayerDeathEvent { player, _ -> breachActive.onPlayerDeath(player) })
                activity.listen(BlockBreakEvent.EVENT, BlockBreakEvent { player, world, blockPos ->
                    breachActive.onBreakBlock(player, world, blockPos) })

                activity.listen(PlayerAttackEntityEvent.EVENT, PlayerAttackEntityEvent { attacker, hand, attacked, hitResult ->
                    if (attacked is AbstractMorphEntity && attacker is ServerPlayerEntity) {
                        breachActive.morphManager.morphPlayer(attacker, attacked)
                        ActionResult.CONSUME
                    }
                    EventResult.PASS
                })

                activity.listen(GameActivityEvents.ENABLE, GameActivityEvents.Enable { breachActive.start() })
                activity.listen(GameActivityEvents.DISABLE, GameActivityEvents.Disable { breachActive.onDisable() })
            }
        }
    }

    private val targetsState = BreachTargetsState(map.targets)
    private val gameSidebar = Sidebar(Sidebar.Priority.MEDIUM)
    private val spawnLogic = BreachActiveSpawnLogic(world, map, players, persistentState,
        config.attackerKits.getKits(), config.defenderKits.getKits(), config.teamOptions.giveHelmets)
    private val roundTimer: BreachRoundTimer
    private val morphManager: MorphManager = MorphManager()
    private val sneakingPlayers: MutableSet<PlayerRef> = mutableSetOf()

    /**
     * Used to open UI on first tick instead of during start(). See tick() for explanation.
     */
    private var shouldDisplayUIsNextTick = true

    private val statistics: GameStatisticBundle = gameSpace.statistics.bundle(Breach.MOD_ID)

    init {
        val prepTicks = config.timesConfig.prepLengthInSeconds * 20;
        val roundTicks = config.timesConfig.roundLengthInSeconds * 20;
        val ticksBeforeNextRound = config.timesConfig.secondsAfterRoundEndBeforeNext * 20;
        if (prepTicks <= 0 || roundTicks <= 0 || ticksBeforeNextRound <= 0) {
            throw GameOpenException(Text.of("prepTicks, roundTicks, and secondsAfterRoundEndBeforeNext need to be positive"))
        }
        roundTimer = BreachRoundTimer(prepTicks, roundTicks, ticksBeforeNextRound)

        buildSidebar()

        if (config.numberOfTargets < 0 || config.numberOfTargets > map.targets.size) {
            throw GameOpenException(Text.of("Invalid number of targets"))
        }
    }

    private fun buildSidebar() {
        gameSidebar.title = Text.translatable("sidebar.breach.title")
        gameSidebar.set {
            for (i in 0 until config.numberOfTargets) {
                it.add { player ->
                    val selectedTargets = targetsState.selected()
                    if (!canSeeTargets(player)) Text.of("???")
                    else if (i >= selectedTargets.size) Text.of("...")
                    else if (targetsState.isBroken(selectedTargets[i]))
                        selectedTargets[i].block.name.formatted(Formatting.STRIKETHROUGH)
                    else selectedTargets[i].block.name
                }
            }
            it.add(Text.of(""))
            it.add { _ ->
                players.getFirstSidebarLine()
            }
            it.add { _ ->
                players.getSecondSidebarLine()
            }
            it.add { _ ->
                Text.of(roundTimer.displayTime())
            }
        }
    }

    private fun tick() {
        morphManager.tick(world)

        // This code used to belong to start(), but for some reason the update to 1.21.4 caused the UIs to no longer
        // open. This hack opens the UIs on the first tick instead, which fixes it for some reason.
        if (shouldDisplayUIsNextTick) {
            for (player in players.onlineParticipants()) {
                if (players.isAnyAttacker(player)) {
                    openSpawnSelectorUIIfMoreThanOneLocation(player, map.attackerSpawnRegions)
                    player.sendMessage(Text.translatable("text.breach.can_reopen_spawn_selection"))
                } else if (players.isAnyDefender(player)) {
                    TargetSelectorUI.open(player, map.targets) { target ->
                        trySelectTarget(player, target)
                    }
                }
            }
            shouldDisplayUIsNextTick = false
        }

        for (player in players.onlineParticipants()) {
            if (player.isSneaking && PlayerRef.of(player) !in sneakingPlayers) {
                onSneak(player)
                sneakingPlayers.add(PlayerRef.of(player))
            } else if (!player.isSneaking && PlayerRef.of(player) in sneakingPlayers) {
                sneakingPlayers.remove(PlayerRef.of(player))
            }
        }

        targetsState.updateBrokenTargets(world) { brokenTarget ->
            world.players.broadcast(brokenTarget.block.name.append(Text.translatable("text.breach.target_was_broken")))
        }
        if (config.outlineTargets) {
            targetsState.updateOutlines(world)
        }
        val phaseThatJustEnded: BreachRoundTimer.Phase? = roundTimer.tick()
        when (phaseThatJustEnded) {
            BreachRoundTimer.Phase.PREP_PHASE -> onEndOfPrepPhase()
            BreachRoundTimer.Phase.MAIN_PHASE -> {}
            BreachRoundTimer.Phase.GAME_END -> finish()
            null -> {}
        }

        if (!roundTimer.isGameEnded()) {
            val defendersMeetWinCondition: Boolean =
                phaseThatJustEnded == BreachRoundTimer.Phase.MAIN_PHASE ||
                        (players.survivingOnlineAttackers().isEmpty() && config.gameplayOptions.endRoundWhenATeamIsEliminated)
            val attackersMeetWinCondition: Boolean =
                (targetsState.allBroken() && !roundTimer.isPrepPhase()) ||
                        (players.survivingOnlineDefenders().isEmpty() && config.gameplayOptions.endRoundWhenATeamIsEliminated)

            if (attackersMeetWinCondition && defendersMeetWinCondition) {
                onTie()
                onGameEnd()
            } else if (attackersMeetWinCondition) {
                onWin(persistentState.teamState.getAttackingTeam())
                onGameEnd()
            } else if (defendersMeetWinCondition) {
                onWin(persistentState.teamState.getDefendingTeam())
                onGameEnd()
            }
        }
    }

    private fun onEndOfPrepPhase() {
        if (targetsState.selected().size < config.numberOfTargets) {
            targetsState.populate(config.numberOfTargets)
            players.survivingOnlineDefenders().broadcast(Text.translatable("text.breach.randomly_selected_targets"))
        }
    }

    private fun onTie() {
        announceGameEnd(Text.translatable("text.breach.tie"))
        for (playerRef in players.allParticipants()) {
            statistics.forPlayer(playerRef).increment(BreachStatistics.ROUNDS_TIED, 1)
        }
    }

    private fun onWin(team: PersistentTeamState.BreachTeam) {
        persistentState.teamState.incrementScore(team)
        val gameTeam = persistentState.teamState.getGameTeam(team)
        val text = gameTeam.config.name.copy()
            .append(Text.translatable("text.breach.win"))
            .formatted(gameTeam.config.chatFormatting())
        announceGameEnd(text)

        for (playerRef in players.allParticipants()) {
            val attackersWon: Boolean = team == persistentState.teamState.getAttackingTeam()
            val onWinningTeam: Boolean =
                attackersWon && players.isAnyAttacker(playerRef) || !attackersWon && players.isAnyDefender(playerRef)
            if (onWinningTeam) {
                statistics.forPlayer(playerRef).increment(BreachStatistics.ROUNDS_WON, 1)
            } else {
                statistics.forPlayer(playerRef).increment(BreachStatistics.ROUNDS_LOST, 1)
            }
        }
    }

    private fun onGameEnd() {
        roundTimer.setGameEnd()
    }

    private fun announceGameEnd(message: Text) {
        for (player in players.onlineParticipants()) {
            player.setTitleTimes(0, 60, 20)
            player.sendTitle(message)
            player.sendSubtitle(persistentState.getScoreDisplay())
        }
    }

    /**
     * Called several seconds after the game ends
     */
    private fun finish() {
        val winningTeam = persistentState.getWinningTeam()
        if (winningTeam == null) {
            // If no one has reached winning score yet
            for (player in world.players) {
                // For some reason clearing in BreachWaiting doesn't work
                player.inventory.clear()
            }

            if (config.teamOptions.swapRolesAfterEachRound) {
                persistentState.teamState.swapRoles()
            }
            BreachWaiting.openInSpace(gameSpace, config, persistentState, players.onlineParticipants(), false)
            gameSpace.worlds.remove(this.world)
        } else {
            // If a team has won enough games
            BreachVictory.open(gameSpace, world, config, persistentState, winningTeam)
        }
    }

    private fun start() {
        gameSidebar.show()
        // TODO: Handle offline players
        for (player in players.onlineParticipants()) {
            gameSidebar.addPlayer(player)

            this.statistics.forPlayer(player).increment(BreachStatistics.ROUNDS_PLAYED, 1)
            if (PlayerRef.of(player) !in persistentState.playersThatPlayedAtLeastOneRound) {
                persistentState.playersThatPlayedAtLeastOneRound.add(PlayerRef.of(player))
                this.statistics.forPlayer(player).increment(StatisticKeys.GAMES_PLAYED, 1)
            }
        }

        spawnLogic.spawnPlayers()

        map.lobbyToRemoveRegion?.bounds?.forEach { blockPos ->
            world.setBlockState(blockPos, Blocks.AIR.defaultState)
        }
    }

    private fun onDisable() {
        gameSidebar.hide()
    }

    private fun openSpawnSelectorUIIfMoreThanOneLocation(player: ServerPlayerEntity, locations: List<TemplateRegion>) {
        if (locations.size <= 1) return

        SpawnSelectorUI.open(player, locations) { ui, selectedRegion ->
            if (roundTimer.isPrepPhase()) {
                val loc = selectedRegion.bounds.randomBottom()
                player.teleportFacingOrigin(world, loc)
            } else {
                player.sendMessage(Text.translatable("text.breach.can_only_select_spawn_in_prep_phase"))
            }
            ui.close()
        }
    }

    private fun canSeeTargets(player: ServerPlayerEntity): Boolean {
        // TODO: Make it so that in prep time, only defenders can see selected targets
        return true;
    }

    private fun trySelectTarget(player: ServerPlayerEntity, target: BreachTarget) {
        if (targetsState.selected().size >= config.numberOfTargets) {
            player.sendMessage(Text.translatable("text.breach.all_targets_selected"))
            return
        }

        if (target in targetsState.selected()) {
            player.sendMessage(Text.translatable("text.breach.target_already_selected"))
            return
        }

        targetsState.selectTarget(target)
        player.sendMessage(Text.translatable("text.breach.target_selected"))
    }

    private fun onPlayerDeath(player: ServerPlayerEntity): EventResult {
        val didEliminate: Boolean = players.eliminate(player)
        spawnLogic.spawnEliminatedPlayer(player)
        if (didEliminate && config.remainingPlayersPopup && !roundTimer.isGameEnded()) {
            displayRemainingPlayersPopup()
            statistics.forPlayer(player).increment(StatisticKeys.DEATHS, 1)
        }
        return EventResult.DENY
    }

    private fun displayRemainingPlayersPopup() {
        val popupMessage: Text = players.getPopupMessage()
        for (player in players.onlineParticipants()) {
            player.setTitleTimes(0, 20, 5)
            player.sendTitle(popupMessage)
        }
    }

    private fun onSneak(player: ServerPlayerEntity) {
        if (roundTimer.isPrepPhase() && players.isSurvivingAttacker(player)) {
            for (region in map.attackerSpawnRegions) {
                if (region.bounds.asBox().contains(player.pos)) {
                    openSpawnSelectorUIIfMoreThanOneLocation(player, map.attackerSpawnRegions)
                    break
                }
            }
        }
    }

    private fun onBreakBlock(player: ServerPlayerEntity, world : ServerWorld, pos: BlockPos): EventResult {
        if (roundTimer.isPrepPhase() && players.isSurvivingAttacker(player)) {
            player.sendMessage(Text.translatable("text.breach.attacker_break_block_in_prep_phase")
                .formatted(Formatting.RED))
            return EventResult.DENY
        }

        if (config.gameplayOptions.beaconsRevealPlayersTime > 0 && world.getBlockState(pos).isOf(Blocks.BEACON)) {
            players.survivingOnlineParticipants().forEach {
                it.addStatusEffect(StatusEffectInstance(StatusEffects.GLOWING, config.gameplayOptions.beaconsRevealPlayersTime))
            }
        }

        return EventResult.PASS
    }
}