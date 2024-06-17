package net.casual.arcade.settings.display

import com.mojang.serialization.Codec
import eu.pb4.sgui.api.elements.GuiElementInterface
import eu.pb4.sgui.api.gui.GuiInterface
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.settings.GameSetting
import net.casual.arcade.settings.SettingListener
import net.casual.arcade.utils.ItemUtils.disableGlint
import net.casual.arcade.utils.ItemUtils.enableGlint
import net.casual.arcade.utils.ItemUtils.hasGlint
import net.casual.arcade.utils.serialization.ArcadeExtraCodecs
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import java.util.*

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

        val selectables = ArrayList<GuiElementInterface>()
        for ((id, data) in this.options) {
            options[id] = data.value
        }

        val setting = this.constructor(this.name, display, options)
        for (data in this.options.values) {
            selectables.add(SettingGuiElement(setting, data))
        }

        setting.override = this.override
        for (listener in this.listeners) {
            setting.addListener(listener)
        }
        return MenuGameSetting(this.display, setting, selectables)
    }

    private class SettingGuiElement<T: Any>(
        private val setting: GameSetting<T>,
        private val data: OptionData<T>
    ): GuiElementInterface {
        private var previous: ItemStack = this.data.default

        override fun getItemStack(): ItemStack {
            return this.previous
        }

        override fun getItemStackForDisplay(gui: GuiInterface): ItemStack {
            val next = this.data.updater(this.setting, this.previous, gui.player)
            this.previous = next
            return next
        }

        override fun getGuiCallback(): GuiElementInterface.ClickCallback {
            return GuiElementInterface.ClickCallback { _, _, _, _ ->
                this.setting.set(this.data.value)
            }
        }
    }

    private data class OptionData<T: Any>(
        val default: ItemStack,
        val value: T,
        val updater: (GameSetting<T>, ItemStack, ServerPlayer) -> ItemStack
    )

    public companion object {
        private val boolean = GameSetting.generator(Codec.BOOL)
        private val integer = GameSetting.generator(Codec.INT)
        private val long = GameSetting.generator(Codec.LONG)
        private val float = GameSetting.generator(Codec.FLOAT)
        private val double = GameSetting.generator(Codec.DOUBLE)
        private val string = GameSetting.generator(Codec.STRING)
        private val id = GameSetting.generator(ResourceLocation.CODEC)
        private val time = GameSetting.generator(ArcadeExtraCodecs.TIME_DURATION)

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

        public fun string(): MenuGameSettingBuilder<String> {
            return MenuGameSettingBuilder(this.string)
        }

        public fun string(block: MenuGameSettingBuilder<String>.() -> Unit): MenuGameSetting<String> {
            return string().apply(block).build()
        }

        public fun id(): MenuGameSettingBuilder<ResourceLocation> {
            return MenuGameSettingBuilder(this.id)
        }

        public fun id(block: MenuGameSettingBuilder<ResourceLocation>.() -> Unit): MenuGameSetting<ResourceLocation> {
            return id().apply(block).build()
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
            return MenuGameSettingBuilder { name, value, options ->
                GameSetting(name, value, options, ArcadeExtraCodecs.enum(options))
            }
        }

        public fun <E: Enum<E>> enumeration(
            block: MenuGameSettingBuilder<E>.() -> Unit
        ): MenuGameSetting<E> {
            return enumeration<E>().apply(block).build()
        }

        public fun <E: Enum<E>> optionalEnumeration(): MenuGameSettingBuilder<Optional<E>> {
            return MenuGameSettingBuilder { name, value, options ->
                GameSetting(name, value, options, ArcadeExtraCodecs.optionalEnum(options))
            }
        }

        public fun <E: Enum<E>> optionalEnumeration(
            block: MenuGameSettingBuilder<Optional<E>>.() -> Unit
        ): MenuGameSetting<Optional<E>> {
            return optionalEnumeration<E>().apply(block).build()
        }

        public fun <T: Any> enchantWhenSetTo(value: T): (GameSetting<T>, ItemStack, ServerPlayer) -> ItemStack {
            return { setting, stack, _ ->
                if (stack.hasGlint()) {
                    if (setting.get() != value) {
                        stack.disableGlint()
                    }
                } else if (setting.get() == value) {
                    stack.enableGlint()
                }
                stack
            }
        }
    }
}