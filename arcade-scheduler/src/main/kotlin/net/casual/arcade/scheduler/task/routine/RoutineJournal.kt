/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task.routine

import com.mojang.serialization.Codec
import com.mojang.serialization.Dynamic
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.casual.arcade.utils.serialization.codec.ArcadeExtraCodecs
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import kotlin.jvm.optionals.getOrNull

/**
 * A record of the suspension points a [Routine] has already passed.
 */
internal class RoutineJournal private constructor(
    private val entries: Int2ObjectMap<Entry>,
    cursor: Int
) {
    /**
     * The index of the suspension point the routine is currently
     * waiting at, or -1 if the routine has not started yet.
     */
    var cursor: Int = cursor
        private set

    fun suspendedAt(index: Int) {
        this.cursor = index
    }

    fun record(index: Int, kind: Kind, id: String?, value: Value? = null) {
        this.entries.put(index, Entry(kind, id, value))
    }

    fun entry(index: Int): Entry? {
        return this.entries.get(index)
    }

    fun verify(index: Int, kind: Kind, id: String?): String? {
        val entry = this.entry(index) ?: return null
        if (entry.kind != kind) {
            return "expected ${entry.kind} at index $index but body issued $kind"
        }
        if (entry.id != null && id != null && entry.id != id) {
            return "expected step '${entry.id}' at index $index but body issued '$id'"
        }
        return null
    }

    fun serialize(output: ValueOutput) {
        output.putInt("cursor", this.cursor)
        val list = output.childrenList("entries")
        for (entry in this.entries.int2ObjectEntrySet()) {
            val child = list.addChild()
            child.putInt("index", entry.intKey)
            val (kind, id, value) = entry.value
            child.store("kind", Kind.CODEC, kind)
            if (id != null) {
                child.putString("id", id)
            }
            value?.write(child)
        }
    }

    data class Entry(val kind: Kind, val id: String?, val value: Value?)

    sealed interface Value {
        fun write(output: ValueOutput)

        fun <T> decode(codec: Codec<T>): T?

        companion object {
            fun <T> of(codec: Codec<T>, value: T): Value? {
                @Suppress("UNCHECKED_CAST")
                return Recorded(codec as Codec<Any>, value ?: return null)
            }
        }
    }

    private class Recorded(private val codec: Codec<Any>, private val value: Any): Value {
        override fun write(output: ValueOutput) {
            output.store("value", this.codec, this.value)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T> decode(codec: Codec<T>): T {
            return this.value as T
        }
    }

    private class Restored(private val dynamic: Dynamic<*>): Value {
        override fun write(output: ValueOutput) {
            output.store("value", Codec.PASSTHROUGH, this.dynamic)
        }

        override fun <T> decode(codec: Codec<T>): T? {
            return codec.parse(this.dynamic).result().orElse(null)
        }
    }

    enum class Kind {
        Delay,
        Step;

        companion object {
            val CODEC = ArcadeExtraCodecs.enum<Kind>()
        }
    }

    companion object {
        fun create(): RoutineJournal {
            return RoutineJournal(Int2ObjectOpenHashMap(), -1)
        }

        fun deserialize(input: ValueInput): RoutineJournal {
            val entries = Int2ObjectOpenHashMap<Entry>()
            for (child in input.childrenListOrEmpty("entries")) {
                val index = child.getInt("index").getOrNull() ?: continue
                val kind = child.read("kind", Kind.CODEC).getOrNull() ?: continue
                val value = child.read("value", Codec.PASSTHROUGH).getOrNull()?.let(::Restored)
                entries.put(index, Entry(kind, child.getString("id").getOrNull(), value))
            }
            return RoutineJournal(entries, input.getIntOr("cursor", -1))
        }
    }
}
