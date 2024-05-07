package net.casual.arcade.settings.display

import eu.pb4.sgui.api.gui.GuiInterface
import net.casual.arcade.gui.screen.SelectionGuiBuilder
import net.casual.arcade.gui.screen.SelectionGuiStyle
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.ScreenUtils
import net.casual.arcade.utils.ScreenUtils.DefaultSettingsComponents
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

public open class DisplayableSettingsDefaults {
    public open fun createSettingsGuiBuilder(player: ServerPlayer): SelectionGuiBuilder {
        return SelectionGuiBuilder(player, ScreenUtils.DefaultMinigameSettingsComponent)
    }

    public open fun createOptionsGuiBuilder(parent: GuiInterface, setting: MenuGameSetting<*>): SelectionGuiBuilder {
        return SelectionGuiBuilder(parent, DefaultSettingsComponents(setting))
            .style(SelectionGuiStyle.centered(setting.optionCount))
    }

    public open fun options(
        builder: MenuGameSettingBuilder<Boolean>,
        enabled: ItemStack = Items.GREEN_STAINED_GLASS_PANE.named("On"),
        disabled: ItemStack = Items.RED_STAINED_GLASS_PANE.named("Off")
    ) {
        builder.option("enabled", enabled, true)
        builder.option("disabled", disabled, false)
    }

    public open fun <E: Enum<E>> options(
        builder: MenuGameSettingBuilder<E>,
        type: Class<E>,
        itemMapper: (enum: E) -> ItemStack = DefaultEnumItemMapper(),
        nameMapper: (enum: E) -> String = { it.name }
    ) {
        for (enum in type.enumConstants) {
            val name = nameMapper(enum)
            val stack = itemMapper(enum)
            builder.option(name, stack, enum)
        }
    }

    private class DefaultEnumItemMapper<E: Enum<E>>: (E) -> ItemStack {
        private var calls = 0

        override fun invoke(enum: E): ItemStack {
            val item = if (this.calls++ % 2 == 0) Items.WHITE_STAINED_GLASS_PANE else Items.RED_STAINED_GLASS_PANE
            return item.named(enum.name)
        }
    }
}