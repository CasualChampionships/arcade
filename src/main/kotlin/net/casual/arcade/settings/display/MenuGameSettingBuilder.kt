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
import org.apache.commons.lang3.mutable.MutableObject

public class MenuGameSettingBuilder<T: Any>(
    private val constructor: (String, T, Map<String, T>) -> GameSetting<T>
) {
    private val options = LinkedHashMap<String, OptionData<T>>()

    private val listeners = ArrayList<SettingListener<T>>()

    public var name: String = ""
    public var display: ItemStack = ItemStack.EMPTY
    public var value: T? = null
    public var override: (ServerPlayer) -> T? = { null }

    public fun name(name: String): MenuGameSettingBuilder<T> {
        this.name = name
        return this
    }

    public fun display(stack: ItemStack): MenuGameSettingBuilder<T> {
        this.display = stack
        return this
    }

    public fun value(value: T): MenuGameSettingBuilder<T> {
        this.value = value
        return this
    }

    public fun option(
        name: String,
        stack: ItemStack,
        value: T,
        updater: (GameSetting<T>, ItemStack, ServerPlayer) -> ItemStack = enchantWhenSetTo(value)
    ): MenuGameSettingBuilder<T> {
        this.options[name] = OptionData(stack, value, updater)
        return this
    }

    public fun listener(listener: SettingListener<T>): MenuGameSettingBuilder<T> {
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

        val selectables = ArrayList<SelectableMenuItem>()
        for ((id, data) in this.options) {
            options[id] = data.value
        }

        val setting = this.constructor(this.name, display, options)
        for (data in this.options.values) {
            val (stack, value, action) = data
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

        public fun bool(): MenuGameSettingBuilder<Boolean> {
            return MenuGameSettingBuilder(this.boolean)
        }

        public fun bool(block: MenuGameSettingBuilder<Boolean>.() -> Unit): MenuGameSetting<Boolean> {
            return bool().apply(block).build()
        }

        public fun int32(): MenuGameSettingBuilder<Int> {
            return MenuGameSettingBuilder(this.integer)
        }

        public fun int32(block: MenuGameSettingBuilder<Int>.() -> Unit): MenuGameSetting<Int> {
            return int32().apply(block).build()
        }

        public fun int64(): MenuGameSettingBuilder<Long> {
            return MenuGameSettingBuilder(this.long)
        }

        public fun int64(block: MenuGameSettingBuilder<Long>.() -> Unit): MenuGameSetting<Long> {
            return int64().apply(block).build()
        }

        public fun float32(): MenuGameSettingBuilder<Float> {
            return MenuGameSettingBuilder(this.float)
        }

        public fun float32(block: MenuGameSettingBuilder<Float>.() -> Unit): MenuGameSetting<Float> {
            return float32().apply(block).build()
        }

        public fun float64(): MenuGameSettingBuilder<Double> {
            return MenuGameSettingBuilder(this.double)
        }

        public fun float64(block: MenuGameSettingBuilder<Double>.() -> Unit): MenuGameSetting<Double> {
            return float64().apply(block).build()
        }

        public fun time(): MenuGameSettingBuilder<MinecraftTimeDuration> {
            return MenuGameSettingBuilder(this.time)
        }

        public fun time(
            block: MenuGameSettingBuilder<MinecraftTimeDuration>.() -> Unit
        ): MenuGameSetting<MinecraftTimeDuration> {
            return time().apply(block).build()
        }

        public fun <E: Enum<E>> enumeration(): MenuGameSettingBuilder<E> {
            return MenuGameSettingBuilder(::EnumGameSetting)
        }

        public fun <E: Enum<E>> enumeration(
            block: MenuGameSettingBuilder<E>.() -> Unit
        ): MenuGameSetting<E> {
            return enumeration<E>().apply(block).build()
        }

        public fun <T: Any> enchantWhenSetTo(value: T): (GameSetting<T>, ItemStack, ServerPlayer) -> ItemStack {
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