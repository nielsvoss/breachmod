package com.nielsvoss.breachmod.data

import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.DyedColorComponent
import net.minecraft.component.type.UnbreakableComponent
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.scoreboard.AbstractTeam
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import xyz.nucleoid.plasmid.api.game.common.team.GameTeam
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamConfig
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey
import xyz.nucleoid.plasmid.api.util.PlayerRef

/**
 * The state that persists between rounds of a Breach game (but not between games)
 */
class RoundPersistentState(private val scoreNeededToWin: Int, private var isTeam1Attacking: Boolean) {
    val kitSelections = KitSelections()
    val team1 = createTeam("Breach1", Text.translatable("team.breach.red"), DyeColor.RED)
    val team2 = createTeam("Breach2", Text.translatable("team.breach.blue"), DyeColor.BLUE)
    private var team1Score: Int = 0
    private var team2Score: Int = 0
    private val displayTeam1First: Boolean = true

    val team1Members: MutableList<PlayerRef> = mutableListOf()
    val team2Members: MutableList<PlayerRef> = mutableListOf()

    private val team1ArmorColor: Int = 0xFF0000
    private val team2ArmorColor: Int = 0x0000FF

    init {
        if (scoreNeededToWin <= 0) throw IllegalArgumentException("scoreNeededToWin must be positive")
    }

    fun getAttackingTeam(): GameTeam = if (isTeam1Attacking) team1 else team2
    fun getDefendingTeam(): GameTeam = if (isTeam1Attacking) team2 else team1
    fun getAttackingTeamMembers(): MutableList<PlayerRef> = if (isTeam1Attacking) team1Members else team2Members
    fun getDefendingTeamMembers(): MutableList<PlayerRef> = if (isTeam1Attacking) team2Members else team1Members
    fun getTeamToDisplayFirst(): GameTeam = if (displayTeam1First) team1 else team2
    fun getTeamToDisplaySecond(): GameTeam = if (displayTeam1First) team2 else team1
    fun swapRoles() {
        isTeam1Attacking = !isTeam1Attacking
    }

    fun incrementAttackerScore() {
        if (isTeam1Attacking) team1Score++ else team2Score++
    }

    fun incrementDefenderScore() {
        if (isTeam1Attacking) team2Score++ else team1Score++
    }

    fun getScoreDisplay(): Text {
        return Text.of("Current score: $team1Score - $team2Score")
    }

    fun getWinningTeam(): GameTeam? {
        return if (team1Score >= scoreNeededToWin) team1
        else if (team2Score >= scoreNeededToWin) team2
        else null
    }

    fun giveTeamArmor(player: ServerPlayerEntity, includeHelmet: Boolean) {
        if (PlayerRef.of(player) in team1Members || PlayerRef.of(player) in team2Members) {
            val armorColor: Int = if (PlayerRef.of(player) in team1Members) team1ArmorColor else team2ArmorColor

            val helmet = ItemStack(Items.LEATHER_HELMET)
            helmet.set(DataComponentTypes.UNBREAKABLE, UnbreakableComponent(true))
            helmet.set(DataComponentTypes.DYED_COLOR, DyedColorComponent(armorColor, false))

            val chestplate = ItemStack(Items.LEATHER_CHESTPLATE)
            chestplate.set(DataComponentTypes.UNBREAKABLE, UnbreakableComponent(true))
            chestplate.set(DataComponentTypes.DYED_COLOR, DyedColorComponent(armorColor, false))

            val leggings = ItemStack(Items.LEATHER_LEGGINGS)
            leggings.set(DataComponentTypes.UNBREAKABLE, UnbreakableComponent(true))
            leggings.set(DataComponentTypes.DYED_COLOR, DyedColorComponent(armorColor, false))

            val boots = ItemStack(Items.LEATHER_BOOTS)
            boots.set(DataComponentTypes.UNBREAKABLE, UnbreakableComponent(true))
            boots.set(DataComponentTypes.DYED_COLOR, DyedColorComponent(armorColor, false))

            if (includeHelmet) player.equipStack(EquipmentSlot.HEAD, helmet)
            player.equipStack(EquipmentSlot.CHEST, chestplate)
            player.equipStack(EquipmentSlot.LEGS, leggings)
            player.equipStack(EquipmentSlot.FEET, boots)
        } else {
            if (includeHelmet) player.equipStack(EquipmentSlot.HEAD, ItemStack(Items.AIR))
            player.equipStack(EquipmentSlot.CHEST, ItemStack(Items.AIR))
            player.equipStack(EquipmentSlot.LEGS, ItemStack(Items.AIR))
            player.equipStack(EquipmentSlot.FEET, ItemStack(Items.AIR))
        }
    }
}

private fun createTeam(id: String, name: Text, color: DyeColor): GameTeam {
    return GameTeam(
        GameTeamKey(id),
        GameTeamConfig.builder()
            .setCollision(AbstractTeam.CollisionRule.NEVER)
            .setColors(GameTeamConfig.Colors.from(color))
            .setName(name)
            .build())
}
