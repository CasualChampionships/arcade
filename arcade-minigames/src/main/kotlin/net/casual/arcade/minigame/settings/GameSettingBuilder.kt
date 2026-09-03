/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.settings

import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.casual.arcade.utils.time.MinecraftTimeDuration
import java.util.*

public class GameSettingBuilder<T: Any>(
    private val type: GameSettingType<T>
) {
    private val options = LinkedHashMap<String, GameSetting.Option<T>>()
    private val overrides = ArrayList<(ServerPlayer) -> T?>()
    private val listeners = ArrayList<SettingListener<T>>()
    private val appliers = ArrayList<SettingApplier<T>>()

    public var name: String = ""
    public var display: ItemStack? = null
    public var value: T? = null

    public fun name(name: String): GameSettingBuilder<T> {
        this.name = name
        return this
    }

    public fun display(stack: ItemStack): GameSettingBuilder<T> {
        this.display = stack
        return this
    }

    public fun value(value: T): GameSettingBuilder<T> {
        this.value = value
        return this
    }

    public fun option(id: String, value: T): GameSettingBuilder<T> {
        return this.option(id, Component.literal(id), value)
    }

    public fun option(id: String, name: Component, value: T): GameSettingBuilder<T> {
        this.options[id] = GameSetting.Option(id, name, value)
        return this
    }

    public fun override(override: (ServerPlayer) -> T?): GameSettingBuilder<T> {
        this.overrides.add(override)
        return this
    }

    public fun onChange(listener: SettingListener<T>): GameSettingBuilder<T> {
        this.listeners.add(listener)
        return this
    }

    public fun onApply(applier: SettingApplier<T>): GameSettingBuilder<T> {
        this.appliers.add(applier)
        return this
    }

    public fun build(): GameSetting<T> {
        check(this.name.isNotEmpty()) { "GameSetting name was not set" }
        val value = checkNotNull(this.value) { "GameSetting ${this.name} value was not set" }
        return GameSetting(
            this.name,
            this.type,
            this.options.values.toList(),
            this.display,
            this.overrides.toList(),
            this.listeners,
            this.appliers,
            value
        )
    }

    public companion object {
        public fun bool(): GameSettingBuilder<Boolean> {
            return GameSettingBuilder(GameSettingType.BOOL)
        }

        public fun bool(block: GameSettingBuilder<Boolean>.() -> Unit): GameSetting<Boolean> {
            return build(bool(), block)
        }

        public fun int32(): GameSettingBuilder<Int> {
            return GameSettingBuilder(GameSettingType.INT32)
        }

        public fun int32(block: GameSettingBuilder<Int>.() -> Unit): GameSetting<Int> {
            return build(int32(), block)
        }

        public fun int64(): GameSettingBuilder<Long> {
            return GameSettingBuilder(GameSettingType.INT64)
        }

        public fun int64(block: GameSettingBuilder<Long>.() -> Unit): GameSetting<Long> {
            return build(int64(), block)
        }

        public fun float32(): GameSettingBuilder<Float> {
            return GameSettingBuilder(GameSettingType.FLOAT32)
        }

        public fun float32(block: GameSettingBuilder<Float>.() -> Unit): GameSetting<Float> {
            return build(float32(), block)
        }

        public fun float64(): GameSettingBuilder<Double> {
            return GameSettingBuilder(GameSettingType.FLOAT64)
        }

        public fun float64(block: GameSettingBuilder<Double>.() -> Unit): GameSetting<Double> {
            return build(float64(), block)
        }

        public fun string(): GameSettingBuilder<String> {
            return GameSettingBuilder(GameSettingType.STRING)
        }

        public fun string(block: GameSettingBuilder<String>.() -> Unit): GameSetting<String> {
            return build(string(), block)
        }

        public fun id(): GameSettingBuilder<Identifier> {
            return GameSettingBuilder(GameSettingType.IDENTIFIER)
        }

        public fun id(block: GameSettingBuilder<Identifier>.() -> Unit): GameSetting<Identifier> {
            return build(id(), block)
        }

        public fun time(): GameSettingBuilder<MinecraftTimeDuration> {
            return GameSettingBuilder(GameSettingType.TIME)
        }

        public fun time(
            block: GameSettingBuilder<MinecraftTimeDuration>.() -> Unit
        ): GameSetting<MinecraftTimeDuration> {
            return build(time(), block)
        }

        public inline fun <reified E: Enum<E>> enumeration(): GameSettingBuilder<E> {
            return GameSettingBuilder(GameSettingType.enumeration<E>())
        }

        public inline fun <reified E: Enum<E>> enumeration(
            block: GameSettingBuilder<E>.() -> Unit
        ): GameSetting<E> {
            val builder = enumeration<E>()
            builder.block()
            return builder.build()
        }

        public inline fun <reified E: Enum<E>> optionalEnumeration(): GameSettingBuilder<Optional<E>> {
            return GameSettingBuilder(GameSettingType.optionalEnumeration<E>())
        }

        public inline fun <reified E: Enum<E>> optionalEnumeration(
            block: GameSettingBuilder<Optional<E>>.() -> Unit
        ): GameSetting<Optional<E>> {
            val builder = optionalEnumeration<E>()
            builder.block()
            return builder.build()
        }

        public fun <T: Any> build(
            builder: GameSettingBuilder<T>,
            block: GameSettingBuilder<T>.() -> Unit
        ): GameSetting<T> {
            builder.block()
            return builder.build()
        }
    }
}
