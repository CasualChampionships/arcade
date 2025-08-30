/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.serialization.json

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import com.mojang.serialization.MapCodec
import net.minecraft.core.HolderLookup
import net.minecraft.util.ProblemReporter
import net.minecraft.world.level.storage.TagValueOutput.EncodeToFieldFailedProblem
import net.minecraft.world.level.storage.TagValueOutput.EncodeToListFailedProblem
import net.minecraft.world.level.storage.TagValueOutput.EncodeToMapFailedProblem
import net.minecraft.world.level.storage.ValueOutput

public class JsonValueOutput private constructor(
    private val reporter: ProblemReporter,
    private val ops: DynamicOps<JsonElement>,
    private val output: JsonObject
): ValueOutput {
    override fun <T: Any> store(key: String, codec: Codec<T>, value: T) {
        when (val result = codec.encodeStart(this.ops, value)) {
            is DataResult.Success -> this.output.add(key, result.value)
            is DataResult.Error -> {
                this.reporter.report(EncodeToFieldFailedProblem(key, value, result))
                result.partialValue.ifPresent { element -> this.output.add(key, element) }
            }
        }
    }

    override fun <T: Any> storeNullable(key: String, codec: Codec<T>, value: T?) {
        if (value != null) {
            this.store(key, codec, value)
        }
    }

    @Deprecated("Values should be stored with keys instead")
    override fun <T: Any> store(codec: MapCodec<T>, value: T) {
        when (val result = codec.encoder().encodeStart(this.ops, value)) {
            is DataResult.Success -> this.output.merge(result.value.asJsonObject)
            is DataResult.Error -> {
                this.reporter.report(EncodeToMapFailedProblem(value, result))
                result.partialValue.ifPresent { element -> this.output.merge(element.asJsonObject) }
            }
        }
    }

    override fun putBoolean(key: String, value: Boolean) {
        this.output.addProperty(key, value)
    }

    override fun putByte(key: String, value: Byte) {
        this.output.addProperty(key, value)
    }

    override fun putShort(key: String, value: Short) {
        this.output.addProperty(key, value)
    }

    override fun putInt(key: String, value: Int) {
        this.output.addProperty(key, value)
    }

    override fun putLong(key: String, value: Long) {
        this.output.addProperty(key, value)
    }

    override fun putFloat(key: String, value: Float) {
        this.output.addProperty(key, value)
    }

    override fun putDouble(key: String, value: Double) {
        this.output.addProperty(key, value)
    }

    override fun putString(key: String, value: String) {
        this.output.addProperty(key, value)
    }

    override fun putIntArray(key: String, value: IntArray) {
        val json = JsonArray(value.size)
        value.forEach(json::add)
        this.output.add(key, json)
    }

    override fun child(key: String): ValueOutput {
        val child = JsonObject()
        this.output.add(key, child)
        return JsonValueOutput(this.reporterForChild(key), this.ops, child)
    }

    override fun childrenList(key: String): ValueOutput.ValueOutputList {
        val child = JsonArray()
        this.output.add(key, child)
        return ListWrapper(key, this.reporter, this.ops, child)
    }

    override fun <T: Any> list(key: String, elementCodec: Codec<T>): ValueOutput.TypedOutputList<T> {
        val child = JsonArray()
        this.output.add(key, child)
        return TypedListWrapper(key, this.reporter, this.ops, child, elementCodec)
    }

    override fun discard(key: String) {
        this.output.remove(key)
    }

    override fun isEmpty(): Boolean {
        return this.output.isEmpty
    }

    public fun buildResult(): JsonObject {
        return this.output
    }

    private fun reporterForChild(name: String): ProblemReporter {
        return this.reporter.forChild(ProblemReporter.FieldPathElement(name))
    }

    private class ListWrapper(
        val name: String,
        val reporter: ProblemReporter,
        val ops: DynamicOps<JsonElement>,
        val output: JsonArray
    ): ValueOutput.ValueOutputList {
        override fun addChild(): ValueOutput {
            val size = this.output.size()
            val child = JsonObject()
            this.output.add(child)
            val reporter = this.reporter.forChild(ProblemReporter.IndexedFieldPathElement(this.name, size))
            return JsonValueOutput(reporter, this.ops, child)
        }

        override fun discardLast() {
            this.output.remove(this.output.size() - 1)
        }

        override fun isEmpty(): Boolean {
            return this.output.isEmpty
        }
    }

    private class TypedListWrapper<T: Any>(
        val name: String,
        val reporter: ProblemReporter,
        val ops: DynamicOps<JsonElement>,
        val output: JsonArray,
        val codec: Codec<T>
    ): ValueOutput.TypedOutputList<T> {
        override fun add(element: T) {
            when (val result = this.codec.encodeStart(this.ops, element)) {
                is DataResult.Success -> this.output.add(result.value)
                is DataResult.Error -> {
                    this.reporter.report(EncodeToListFailedProblem(this.name, element, result))
                    result.partialValue.ifPresent(this.output::add)
                }
            }
        }

        override fun isEmpty(): Boolean {
            return this.output.isEmpty
        }
    }

    public companion object {
        @JvmStatic
        public fun create(reporter: ProblemReporter, lookup: HolderLookup.Provider): JsonValueOutput {
            return JsonValueOutput(reporter, lookup.createSerializationContext(JsonOps.INSTANCE), JsonObject())
        }

        @JvmStatic
        public fun create(reporter: ProblemReporter): JsonValueOutput {
            return JsonValueOutput(reporter, JsonOps.INSTANCE, JsonObject())
        }
    }
}