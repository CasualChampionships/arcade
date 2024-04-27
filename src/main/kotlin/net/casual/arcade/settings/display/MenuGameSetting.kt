package net.casual.arcade.settings.display

import eu.pb4.sgui.api.elements.GuiElementInterface
import net.casual.arcade.settings.GameSetting
import net.minecraft.world.item.ItemStack

public class MenuGameSetting<T: Any>(
    public val display: ItemStack,
    public val setting: GameSetting<T>,
    public val options: List<GuiElementInterface>
) {
    public val optionCount: Int
        get() = this.options.size
}