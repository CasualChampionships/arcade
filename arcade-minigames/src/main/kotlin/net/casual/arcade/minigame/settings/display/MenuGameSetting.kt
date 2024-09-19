package net.casual.arcade.minigame.settings.display

import eu.pb4.sgui.api.elements.GuiElementInterface
import net.casual.arcade.minigame.settings.GameSetting
import net.minecraft.world.item.ItemStack

public class MenuGameSetting<T: Any>(
    public val display: ItemStack,
    public val setting: GameSetting<T>,
    public val options: List<GuiElementInterface>
) {
    public val optionCount: Int
        get() = this.options.size
}