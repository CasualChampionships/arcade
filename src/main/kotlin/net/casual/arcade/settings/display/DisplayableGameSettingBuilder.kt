package net.casual.arcade.settings.display

import net.casual.arcade.items.HashableItemStack
import net.casual.arcade.settings.GameSetting
import net.casual.arcade.settings.SettingListener
import net.casual.arcade.settings.impl.*
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
        public fun bool(): DisplayableGameSettingBuilder<Boolean> {
            return DisplayableGameSettingBuilder(::BooleanGameSetting)
        }

        public fun bool(block: DisplayableGameSettingBuilder<Boolean>.() -> Unit): DisplayableGameSetting<Boolean> {
            return DisplayableGameSettingBuilder(::BooleanGameSetting).apply(block).build()
        }

        public fun int32(): DisplayableGameSettingBuilder<Int> {
            return DisplayableGameSettingBuilder(::IntegerGameSetting)
        }

        public fun int32(block: DisplayableGameSettingBuilder<Int>.() -> Unit): DisplayableGameSetting<Int> {
            return DisplayableGameSettingBuilder(::IntegerGameSetting).apply(block).build()
        }

        public fun int64(): DisplayableGameSettingBuilder<Long> {
            return DisplayableGameSettingBuilder(::LongGameSetting)
        }

        public fun int64(block: DisplayableGameSettingBuilder<Long>.() -> Unit): DisplayableGameSetting<Long> {
            return DisplayableGameSettingBuilder(::LongGameSetting).apply(block).build()
        }

        public fun float64(): DisplayableGameSettingBuilder<Double> {
            return DisplayableGameSettingBuilder(::DoubleGameSetting)
        }

        public fun float64(block: DisplayableGameSettingBuilder<Double>.() -> Unit): DisplayableGameSetting<Double> {
            return DisplayableGameSettingBuilder(::DoubleGameSetting).apply(block).build()
        }

        public fun <E: Enum<E>> enumeration(): DisplayableGameSettingBuilder<E> {
            return DisplayableGameSettingBuilder(::EnumGameSetting)
        }

        public fun <E: Enum<E>> enumeration(
            block: DisplayableGameSettingBuilder<E>.() -> Unit
        ): DisplayableGameSetting<E> {
            return DisplayableGameSettingBuilder<E>(::EnumGameSetting).apply(block).build()
        }
    }
}