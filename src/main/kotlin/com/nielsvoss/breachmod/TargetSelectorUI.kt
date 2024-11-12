package com.nielsvoss.breachmod

import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.function.Consumer

class TargetSelectorUI(player: ServerPlayerEntity, private val availableTargets: List<BreachTarget>,
                       private val onSelectTarget: Consumer<BreachTarget>)
    : SimpleGui(getType(availableTargets.size), player, availableTargets.size > 53) {

    companion object {
        fun open(player: ServerPlayerEntity, availableTargets: List<BreachTarget>, onSelectTarget: Consumer<BreachTarget>): Boolean {
            val ui = TargetSelectorUI(player, availableTargets, onSelectTarget)
            return ui.open()
        }
    }

    init {
        this.title = Text.translatable("text.breach.select_targets")
    }

    override fun onOpen() {
        for ((pos, target) in availableTargets.withIndex()) {
            val itemStack = ItemStack(target.block.asItem())
            val icon = GuiElementBuilder.from(itemStack)
            icon.addLoreLine(Text.of(target.pos.toString()))

            icon.setCallback(Runnable { onSelectTarget.accept(target) })

            this.setSlot(pos, icon)
        }

        super.onOpen() // Might not do anything right now?
    }
}

// Based on code in
// https://github.com/NucleoidMC/skywars/blob/1.20/src/main/java/us/potatoboy/skywars/game/ui/KitSelectorUI.java
private fun getType(size: Int): ScreenHandlerType<*> {
    return if (size <= 8) {
        ScreenHandlerType.GENERIC_9X1
    } else if (size <= 17) {
        ScreenHandlerType.GENERIC_9X2
    } else if (size <= 26) {
        ScreenHandlerType.GENERIC_9X3
    } else if (size <= 35) {
        ScreenHandlerType.GENERIC_9X4
    } else if (size <= 44) {
        ScreenHandlerType.GENERIC_9X5
    } else {
        ScreenHandlerType.GENERIC_9X6
    }
}
