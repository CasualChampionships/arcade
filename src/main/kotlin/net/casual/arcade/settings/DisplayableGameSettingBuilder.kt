package net.casual.arcade.settings

import net.casual.arcade.items.HashableItemStack
import net.minecraft.world.item.ItemStack

public class DisplayableGameSettingBuilder<T: Any>(
    private val constructor: (String, T, Map<String, T>) -> GameSetting<T>
) {
    private val options = LinkedHashMap<String, T>()
    private val stacks = LinkedHashMap<HashableItemStack, T>()

    private val listeners = ArrayList<SettingListener<T>>()

    public var name: String = ""
    public var display: ItemStack = ItemStack.EMPTY
    public var value: T? = null

    public fun name(name: String): DisplayableGameSettingBuilder<T> {
        this.name = name
        return this
    }

    public fun display(stack: ItemStack): DisplayableGameSettingBuilder<T> {
        this.display = stack
        return this
    }

    public fun value(value: T): DisplayableGameSettingBuilder<T> {
        this.value = value
        return this
    }

    public fun option(name: String, stack: ItemStack, value: T): DisplayableGameSettingBuilder<T> {
        this.options[name] = value
        this.stacks[HashableItemStack(stack)] = value
        return this
    }

    public fun listener(listener: SettingListener<T>): DisplayableGameSettingBuilder<T> {
        this.listeners.add(listener)
        return this
    }

    public fun build(): DisplayableGameSetting<T> {
        if (this.name.isEmpty()) {
            throw IllegalStateException("No name to build GameSetting")
        }
        if (this.display.isEmpty) {
            throw IllegalStateException("No display to build GameSetting")
        }
        val value = this.value ?: throw IllegalStateException("No value to build GameSetting")
        val setting = this.constructor(this.name, value, this.options)
        for (listener in this.listeners) {
            setting.addListener(listener)
        }
        return DisplayableGameSetting(this.display, setting, this.stacks)
    }

    public companion object {
        public fun boolean(): DisplayableGameSettingBuilder<Boolean> {
            return DisplayableGameSettingBuilder(::BooleanGameSetting)
        }

        public fun integer(): DisplayableGameSettingBuilder<Int> {
            return DisplayableGameSettingBuilder(::IntegerGameSetting)
        }

        public fun long(): DisplayableGameSettingBuilder<Long> {
            return DisplayableGameSettingBuilder(::LongGameSetting)
        }

        public fun double(): DisplayableGameSettingBuilder<Double> {
            return DisplayableGameSettingBuilder(::DoubleGameSetting)
        }

        public fun <E: Enum<E>> enum(): DisplayableGameSettingBuilder<E> {
            return DisplayableGameSettingBuilder(::EnumGameSetting)
        }
    }
}