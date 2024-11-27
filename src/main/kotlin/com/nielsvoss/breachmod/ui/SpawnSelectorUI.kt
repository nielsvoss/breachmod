package com.nielsvoss.breachmod.ui;

import com.nielsvoss.breachmod.util.UIUtils
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import xyz.nucleoid.map_templates.TemplateRegion
import java.util.function.BiConsumer

class SpawnSelectorUI private constructor(player: ServerPlayerEntity, private val regions: List<TemplateRegion>,
                                          private val onSelectRegion: BiConsumer<SpawnSelectorUI, TemplateRegion>)
    : SimpleGui(UIUtils.getTypeOrThrowIfTooLarge(regions.size), player, false) {

    companion object {
        fun open(player: ServerPlayerEntity, regions: List<TemplateRegion>, onSelectRegion: BiConsumer<SpawnSelectorUI, TemplateRegion>): Boolean {
            val ui = SpawnSelectorUI(player, regions, onSelectRegion)
            return ui.open()
        }
    }

    override fun onOpen() {
        for ((slotPos, region) in regions.withIndex()) {
            val itemStack = ItemStack(Items.GOLD_NUGGET)
            val icon = GuiElementBuilder.from(itemStack)
            val posToDisplay: Vec3d = region.bounds.centerBottom()
            icon.addLoreLine(Text.of("${posToDisplay.x.toInt()} ${posToDisplay.y.toInt()} ${posToDisplay.z.toInt()}"))

            icon.setCallback(Runnable { onSelectRegion.accept(this, region) })

            this.setSlot(slotPos, icon)
        }

        super.onOpen() // Might not do anything right now?
    }
}
