package net.casual.arcade.settings.display

import net.casual.arcade.gui.screen.SelectableMenuItem
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.settings.GameSetting
import net.casual.arcade.settings.SettingListener
import net.casual.arcade.settings.impl.EnumGameSetting
import net.casual.arcade.utils.ItemUtils.enableGlint
import net.casual.arcade.utils.ItemUtils.removeEnchantments
import net.casual.arcade.utils.json.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

public class DisplayableGameSettingBuilder<T: Any>(
    private val constructor: (String, T, Map<String, T>) -> GameSetting<T>
) {
    private val options = LinkedHashMap<String, OptionData<T>>()

    private val listeners = ArrayList<SettingListener<T>>()

    public var name: String = ""
    public var display: ItemStack = ItemStack.EMPTY
    public var value: T? = null
    public var override: (ServerPlayer) -> T? = { null }

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

    public fun option(
        name: String,
        stack: ItemStack,
        value: T,
        updater: (GameSetting<T>, ItemStack, ServerPlayer) -> ItemStack = defaultUpdater(value)
    ): DisplayableGameSettingBuilder<T> {
        this.options[name] = OptionData(stack, value, updater)
        return this
    }

    public fun listener(listener: SettingListener<T>): DisplayableGameSettingBuilder<T> {
        this.listeners.add(listener)
        return this
    }

    public fun build(): MenuGameSetting<T> {
        if (this.name.isEmpty()) {
            throw IllegalStateException("No name to build GameSetting")
        }
        if (this.display.isEmpty) {
            throw IllegalStateException("No display to build GameSetting")
        }
        val display = this.value ?: throw IllegalStateException("No value to build GameSetting")

        val options = LinkedHashMap<String, T>()
        val setting = this.constructor(this.name, display, options)

        val selectables = ArrayList<SelectableMenuItem>()
        for ((id, data) in this.options) {
            val (stack, value, action) = data
            options[id] = value
            selectables.add(SelectableMenuItem.build {
                default = stack
                onUpdate = { stack, player -> action(setting, stack, player) }
                onSelected = {
                    setting.set(value)
                }
            })
        }

        setting.override = this.override
        for (listener in this.listeners) {
            setting.addListener(listener)
        }
        return MenuGameSetting(this.display, setting, selectables)
    }

    private data class OptionData<T: Any>(
        val default: ItemStack,
        val value: T,
        val updater: (GameSetting<T>, ItemStack, ServerPlayer) -> ItemStack
    )

    public companion object {
        private val boolean = GameSetting.generator(BooleanSerializer)
        private val integer = GameSetting.generator(IntSerializer)
        private val long = GameSetting.generator(LongSerializer)
        private val float = GameSetting.generator(FloatSerializer)
        private val double = GameSetting.generator(DoubleSerializer)
        private val time = GameSetting.generator(TimeDurationSerializer)

        public fun bool(): DisplayableGameSettingBuilder<Boolean> {
            return DisplayableGameSettingBuilder(this.boolean)
        }

        public fun bool(block: DisplayableGameSettingBuilder<Boolean>.() -> Unit): MenuGameSetting<Boolean> {
            return bool().apply(block).build()
        }

        public fun int32(): DisplayableGameSettingBuilder<Int> {
            return DisplayableGameSettingBuilder(this.integer)
        }

        public fun int32(block: DisplayableGameSettingBuilder<Int>.() -> Unit): MenuGameSetting<Int> {
            return int32().apply(block).build()
        }

        public fun int64(): DisplayableGameSettingBuilder<Long> {
            return DisplayableGameSettingBuilder(this.long)
        }

        public fun int64(block: DisplayableGameSettingBuilder<Long>.() -> Unit): MenuGameSetting<Long> {
            return int64().apply(block).build()
        }

        public fun float32(): DisplayableGameSettingBuilder<Float> {
            return DisplayableGameSettingBuilder(this.float)
        }

        public fun float32(block: DisplayableGameSettingBuilder<Float>.() -> Unit): MenuGameSetting<Float> {
            return float32().apply(block).build()
        }

        public fun float64(): DisplayableGameSettingBuilder<Double> {
            return DisplayableGameSettingBuilder(this.double)
        }

        public fun float64(block: DisplayableGameSettingBuilder<Double>.() -> Unit): MenuGameSetting<Double> {
            return float64().apply(block).build()
        }

        public fun time(): DisplayableGameSettingBuilder<MinecraftTimeDuration> {
            return DisplayableGameSettingBuilder(this.time)
        }

        public fun time(
            block: DisplayableGameSettingBuilder<MinecraftTimeDuration>.() -> Unit
        ): MenuGameSetting<MinecraftTimeDuration> {
            return time().apply(block).build()
        }

        public fun <E: Enum<E>> enumeration(): DisplayableGameSettingBuilder<E> {
            return DisplayableGameSettingBuilder(::EnumGameSetting)
        }

        public fun <E: Enum<E>> enumeration(
            block: DisplayableGameSettingBuilder<E>.() -> Unit
        ): MenuGameSetting<E> {
            return enumeration<E>().apply(block).build()
        }

        private fun <T: Any> defaultUpdater(value: T): (GameSetting<T>, ItemStack, ServerPlayer) -> ItemStack {
            return { setting, stack, _ ->
                if (stack.isEnchanted) {
                    if (setting.get() != value) {
                        stack.removeEnchantments()
                    }
                } else if (setting.get() == value) {
                    stack.enableGlint()
                }
                stack
            }
        }
    }
}