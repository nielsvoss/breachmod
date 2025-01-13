package com.nielsvoss.breachmod.data

import com.nielsvoss.breachmod.kit.BreachKit
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import xyz.nucleoid.plasmid.api.util.PlayerRef

class KitSelections {
    private val attackerKitSelections: MutableMap<PlayerRef, BreachKit> = mutableMapOf()
    private val defenderKitSelections: MutableMap<PlayerRef, BreachKit> = mutableMapOf()

    fun setAttackerKit(player: ServerPlayerEntity, kit: BreachKit) {
        attackerKitSelections[PlayerRef.of(player)] = kit
    }

    fun setDefenderKit(player: ServerPlayerEntity, kit: BreachKit) {
        defenderKitSelections[PlayerRef.of(player)] = kit
    }

    /**
     * If player has no kit selected, chooses randomly, and informs player.
     * Only returns null if the player has no kit selected and the available attacker kits is empty.
     */
    fun getAttackerKitOrRandom(player: ServerPlayerEntity, availableAttackerKits: List<BreachKit>): BreachKit? {
        return attackerKitSelections[PlayerRef.of(player)] ?: chooseRandomly(player, availableAttackerKits)
    }

    fun getDefenderKitOrRandom(player: ServerPlayerEntity, availableDefenderKits: List<BreachKit>): BreachKit? {
        return defenderKitSelections[PlayerRef.of(player)] ?: chooseRandomly(player, availableDefenderKits)
    }

    private fun chooseRandomly(player: ServerPlayerEntity, availableKits: List<BreachKit>): BreachKit? {
        val kit: BreachKit? = availableKits.randomOrNull()
        if (kit != null) {
            player.sendMessage(Text.translatable("text.breach.kit_chosen_randomly").append(kit.getName()))
        } else {
            player.sendMessage(Text.translatable("text.breach.no_kit_selected_or_available"))
        }
        return kit
    }
}