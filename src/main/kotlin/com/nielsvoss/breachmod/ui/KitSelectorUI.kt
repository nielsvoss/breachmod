package com.nielsvoss.breachmod.ui;

import com.nielsvoss.breachmod.kit.BreachKit
import com.nielsvoss.breachmod.kit.BreachKitRegistry
import com.nielsvoss.breachmod.util.UIUtils
import eu.pb4.sgui.api.GuiHelpers
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.GuiInterface
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text
import java.util.function.BiConsumer

class KitSelectorUI private constructor(
    player: ServerPlayerEntity,
    private val kits: List<BreachKit>,
    private val titleText: Text,
    private val prev: GuiInterface?,
    private val onSelectKit: BiConsumer<KitSelectorUI, BreachKit>
) : SimpleGui(UIUtils.getTypeOrThrowIfTooLarge(kits.size), player, false) {
    companion object {
        fun open(player: ServerPlayerEntity, kits: List<BreachKit>, title: Text, onSelectKit: BiConsumer<KitSelectorUI, BreachKit>): Boolean {
            val prev: GuiInterface? = GuiHelpers.getCurrentGui(player)
            val ui = KitSelectorUI(player, kits, title, prev, onSelectKit)
            return ui.open()
        }
    }

    init {
        this.title = this.titleText
    }

    override fun onOpen() {
        for ((slotPos, kit) in kits.withIndex()) {
            val icon = GuiElementBuilder.from(kit.getIcon())
            icon.setName(kit.getName())
            icon.setCallback(Runnable { onSelectKit.accept(this, kit) })

            this.setSlot(slotPos, icon)
        }

        super.onOpen() // Might not do anything right now?
    }

    override fun onClose() {
        prev?.open()

        super.onClose() // Not sure if this is needed
    }
}
