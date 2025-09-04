/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.stats

import net.minecraft.core.Holder
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

public class StatTracker {
    private val stats = ConcurrentHashMap<Holder<StatType<*>>, Stat<*>>()
    private var frozen: Boolean = false

    public fun freeze() {
        this.frozen = true
        for (stat in this.stats.values) {
            stat.frozen = true
        }
    }

    public fun unfreeze() {
        this.frozen = false
        for (stat in this.stats.values) {
            stat.frozen = false
        }
    }

    @Suppress("UNCHECKED_CAST")
    public fun <T: Any> getStatValueOrDefault(holder: Holder<StatType<T>>): T {
        val stat = this.stats[holder as Holder<StatType<*>>] ?: return holder.value().default
        return (stat as Stat<T>).value
    }

    @Suppress("UNCHECKED_CAST")
    public fun <T: Any> getOrCreateStat(holder: Holder<StatType<T>>): Stat<T> {
        return this.stats.getOrPut(holder as Holder<StatType<*>>) {
            this.createStat(holder.value())
        } as Stat<T>
    }

    public fun serialize(output: ValueOutput.ValueOutputList) {
        for ((type, stat) in this.stats) {
            val child = output.addChild()
            child.store("type", StatType.HOLDER_CODEC, type)
            child.store("value", stat)
        }
    }

    @Suppress("UNCHECKED_CAST")
    public fun deserialize(input: ValueInput.ValueInputList) {
        for (child in input) {
            val type = child.read("type", StatType.HOLDER_CODEC).getOrNull() ?: continue
            val value = child.read("value", type.value().codec).getOrNull() ?: continue
            val stat = this.getOrCreateStat(type as Holder<StatType<Any>>)
            stat.set(value)
        }
    }

    private fun <T: Any> createStat(type: StatType<T>): Stat<T> {
        return Stat(type).also { stat -> stat.frozen = this.frozen }
    }

    private fun <T: Any> ValueOutput.store(key: String, stat: Stat<T>) {
        this.store(key, stat.type.codec, stat.value)
    }
}