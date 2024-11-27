package com.nielsvoss.breachmod.ui

import com.nielsvoss.breachmod.data.BreachTarget
import com.nielsvoss.breachmod.util.UIUtils
import com.nielsvoss.breachmod.util.toSpaceSeparatedString
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.function.Consumer

class TargetSelectorUI(player: ServerPlayerEntity, private val availableTargets: List<BreachTarget>,
                       private val onSelectTarget: Consumer<BreachTarget>)
    : SimpleGui(UIUtils.getTypeOrThrowIfTooLarge(availableTargets.size), player, false) {

    companion object {
        fun open(player: ServerPlayerEntity, availableTargets: List<BreachTarget>, onSelectTarget: Consumer<BreachTarget>): Boolean {
            val ui = TargetSelectorUI(player, availableTargets, onSelectTarget)
            return ui.open()
        }
    }

    init {
        this.title = Text.translatable("gui.breach.select_targets")
        if (availableTargets.size > 53) {
            throw IllegalArgumentException("Too many targets to be selectable in the GUI")
        }
    }

    override fun onOpen() {
        for ((slotPos, target) in availableTargets.withIndex()) {
            val itemStack = ItemStack(target.block.asItem())
            val icon = GuiElementBuilder.from(itemStack)
            icon.addLoreLine(Text.of(target.pos.toSpaceSeparatedString()))

            icon.setCallback(Runnable { onSelectTarget.accept(target) })

            this.setSlot(slotPos, icon)
        }

        super.onOpen() // Might not do anything right now?
    }
}
