/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.serialization

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.minecraft.core.HolderLookup
import net.minecraft.world.level.storage.ValueInput
import java.util.*
import java.util.stream.Stream

public class EmptyValueInput(private val lookup: HolderLookup.Provider): ValueInput {
    override fun <T: Any> read(key: String, codec: Codec<T>): Optional<T> {
        return Optional.empty()
    }

    @Deprecated("Values should be stored with keys instead", ReplaceWith("Optional.empty()", "java.util.Optional"))
    override fun <T: Any> read(codec: MapCodec<T>): Optional<T> {
        return Optional.empty()
    }

    override fun child(key: String): Optional<ValueInput> {
        return Optional.empty()
    }

    override fun childOrEmpty(key: String): ValueInput {
        return this
    }

    override fun childrenList(key: String): Optional<ValueInput.ValueInputList> {
        return Optional.empty()
    }

    override fun childrenListOrEmpty(key: String): ValueInput.ValueInputList {
        return emptyChildrenList()
    }

    public fun emptyChildrenList(): ValueInput.ValueInputList {
        return ListWrapper
    }

    override fun <T: Any> list(key: String, elementCodec: Codec<T>): Optional<ValueInput.TypedInputList<T>> {
        return Optional.empty()
    }

    override fun <T: Any> listOrEmpty(key: String, elementCodec: Codec<T>): ValueInput.TypedInputList<T> {
        return this.emptyList()
    }

    public fun <T: Any> emptyList(): ValueInput.TypedInputList<T> {
        @Suppress("UNCHECKED_CAST")
        return TypedListWrapper as ValueInput.TypedInputList<T>
    }

    override fun getBooleanOr(key: String, defaultValue: Boolean): Boolean {
        return defaultValue
    }

    override fun getByteOr(key: String, defaultValue: Byte): Byte {
        return defaultValue
    }

    override fun getShortOr(key: String, defaultValue: Short): Int {
        return defaultValue.toInt()
    }

    override fun getInt(key: String): Optional<Int> {
        return Optional.empty()
    }

    override fun getIntOr(key: String, defaultValue: Int): Int {
        return defaultValue
    }

    override fun getLongOr(key: String, defaultValue: Long): Long {
        return defaultValue
    }

    override fun getLong(key: String): Optional<Long> {
        return Optional.empty()
    }

    override fun getFloatOr(key: String, defaultValue: Float): Float {
        return defaultValue
    }

    override fun getDoubleOr(key: String, defaultValue: Double): Double {
        return defaultValue
    }

    override fun getString(key: String): Optional<String> {
        return Optional.empty()
    }

    override fun getStringOr(key: String, defaultValue: String): String {
        return defaultValue
    }

    override fun getIntArray(key: String): Optional<IntArray> {
        return Optional.empty()
    }

    @Deprecated("You should avoid directly using lookups, this may be empty")
    override fun lookup(): HolderLookup.Provider {
        return this.lookup
    }

    public object ListWrapper: ValueInput.ValueInputList {
        override fun iterator(): MutableIterator<ValueInput> {
            return Collections.emptyIterator()
        }

        override fun isEmpty(): Boolean {
            return true
        }

        override fun stream(): Stream<ValueInput> {
            return Stream.empty()
        }
    }

    private object TypedListWrapper: ValueInput.TypedInputList<Any> {
        override fun iterator(): MutableIterator<ValueInput> {
            return Collections.emptyIterator()
        }

        override fun isEmpty(): Boolean {
            return true
        }

        override fun stream(): Stream<Any> {
            return Stream.empty()
        }
    }
}