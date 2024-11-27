package com.nielsvoss.breachmod.ui;

import com.nielsvoss.breachmod.util.UIUtils
import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text
import net.minecraft.util.Identifier
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

    init {
        this.title = Text.translatable("gui.breach.select_spawn")
    }

    override fun onOpen() {
        for ((slotPos, region) in regions.withIndex()) {
            val itemStack = ItemStack(getIconItem(region) ?: Items.GOLD_NUGGET)
            val icon = GuiElementBuilder.from(itemStack)
            val posToDisplay: Vec3d = region.bounds.centerBottom()
            val posToDisplayString = "${posToDisplay.x.toInt()} ${posToDisplay.y.toInt()} ${posToDisplay.z.toInt()}"

            icon.setName(getIconName(region) ?: Text.translatable("gui.breach.default_spawn_location_name", slotPos + 1))
            icon.addLoreLine(Text.of(posToDisplayString))
            icon.setCallback(Runnable { onSelectRegion.accept(this, region) })

            this.setSlot(slotPos, icon)
        }

        super.onOpen() // Might not do anything right now?
    }

    private fun getIconItem(region: TemplateRegion): Item? {
        val iconName = region.data.getString("icon")
        if (iconName == "") return null
        val identifier = Identifier.tryParse(iconName) ?: return null
        if (!Registries.ITEM.containsId(identifier)) return null
        return Registries.ITEM.get(identifier)
    }

    private fun getIconName(region: TemplateRegion): Text? {
        val translationKey = region.data.getString("name")
        if (translationKey == "") return null
        return Text.translatable(translationKey)
    }
}
