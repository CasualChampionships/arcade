package net.casualuhc.arcade.utils

import net.casualuhc.arcade.settings.DisplayableGameSettingBuilder
import net.casualuhc.arcade.utils.ItemUtils.literalNamed
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object SettingsUtils {
    fun DisplayableGameSettingBuilder<Boolean>.defaultOptions(
        enabled: ItemStack = Items.GREEN_STAINED_GLASS_PANE.literalNamed("On"),
        disabled: ItemStack = Items.RED_STAINED_GLASS_PANE.literalNamed("Off")
    ) {
        this.option("enabled", enabled, true)
        this.option("disabled", disabled, false)
    }

    fun <E: Enum<E>> DisplayableGameSettingBuilder<E>.defaultOptions(
        type: Class<E>,
        itemMapper: (enum: E) -> ItemStack = DefaultEnumItemMapper(),
        nameMapper: (enum: E) -> String = { it.name }
    ) {
        for (enum in type.enumConstants) {
            val name = nameMapper(enum)
            val stack = itemMapper(enum)
            this.option(name, stack, enum)
        }
    }

    private class DefaultEnumItemMapper<E: Enum<E>>: (E) -> ItemStack {
        private var calls = 0

        override fun invoke(enum: E): ItemStack {
            val item = if (this.calls++ % 2 == 0) Items.WHITE_STAINED_GLASS_PANE else Items.RED_STAINED_GLASS_PANE
            return item.literalNamed(enum.name)
        }
    }
}