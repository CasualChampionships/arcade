/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.serialization.kotlin

import com.google.gson.internal.NumberLimits
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.MapLike
import kotlinx.serialization.json.*
import java.math.BigDecimal
import java.util.function.Consumer
import java.util.stream.Stream

public object KJsonOps: DynamicOps<JsonElement> {
    override fun empty(): JsonElement {
        return JsonNull
    }

    override fun <U: Any> convertTo(outOps: DynamicOps<U>, input: JsonElement): U {
        return when (input) {
            is JsonObject -> this.convertMap(outOps, input)
            is JsonArray -> this.convertList(outOps, input)
            is JsonPrimitive -> this.convertPrimitiveTo(outOps, input)
        }
    }

    override fun createNumeric(i: Number): JsonElement {
        return JsonPrimitive(i)
    }

    override fun createString(value: String): JsonElement {
        return JsonPrimitive(value)
    }

    override fun createBoolean(value: Boolean): JsonElement {
        return JsonPrimitive(value)
    }

    override fun remove(input: JsonElement, key: String): JsonElement {
        if (input is JsonObject) {
            return JsonObject(input.filterKeys { it != key })
        }
        return input
    }

    override fun createList(input: Stream<JsonElement>): JsonElement {
        return JsonArray(input.toList())
    }

    override fun getStream(input: JsonElement): DataResult<Stream<JsonElement>> {
        if (input !is JsonArray) {
            return DataResult.error { "Not a json array: $input" }
        }
        return DataResult.success(input.stream().map { element -> if (element == JsonNull) null else element })
    }

    override fun getList(input: JsonElement): DataResult<Consumer<Consumer<JsonElement?>>> {
        if (input !is JsonArray) {
            return DataResult.error { "Not a json array: $input" }
        }
        return DataResult.success(Consumer { consumer ->
            for (element in input) {
                consumer.accept(if (element == JsonNull) null else element)
            }
        })
    }

    override fun createMap(map: Stream<Pair<JsonElement, JsonElement>>): JsonElement {
        val result = LinkedHashMap<String, JsonElement>()
        map.forEach { entry ->
            result[entry.first.toString()] = entry.second
        }
        return JsonObject(result)
    }

    override fun getMapValues(input: JsonElement): DataResult<Stream<Pair<JsonElement, JsonElement?>>> {
        if (input !is JsonObject) {
            return DataResult.error { "Not a JSON object: $input" }
        }
        val stream = input.entries.stream().map<Pair<JsonElement, JsonElement?>> { entry ->
            Pair.of(JsonPrimitive(entry.key), if (entry.value == JsonNull) null else entry.value)
        }
        return DataResult.success(stream)
    }

    override fun getMap(input: JsonElement): DataResult<MapLike<JsonElement>> {
        if (input !is JsonObject) {
            return DataResult.error { "Not a JSON object: $input" }
        }

        return DataResult.success(object: MapLike<JsonElement> {
            override fun get(key: JsonElement): JsonElement? {
                if (key is JsonPrimitive) {
                    return this.get(key.content)
                }
                return null
            }

            override fun get(key: String): JsonElement? {
                val value = input[key]
                return if (value == JsonNull) null else value
            }

            override fun entries(): Stream<Pair<JsonElement, JsonElement>> {
                return input.entries.stream().map { (key, value) ->
                    Pair.of(JsonPrimitive(key), value)
                }
            }

            override fun toString(): String {
                return "MapLike[$input]"
            }
        })
    }

    override fun mergeToMap(map: JsonElement, key: JsonElement, value: JsonElement): DataResult<JsonElement> {
        if (map !is JsonObject && map != this.empty()) {
            return DataResult.error({ "mergeToMap called with not a map: $map" }, map)
        }
        if (key !is JsonPrimitive || !key.isString) {
            return DataResult.error({ "key is not a string: $key" }, map)
        }

        val entries = LinkedHashMap<String, JsonElement>()
        if (map != this.empty()) {
            entries.putAll(map.jsonObject)
        }
        entries[key.content] = value
        return DataResult.success(JsonObject(entries))
    }

    override fun mergeToMap(map: JsonElement, values: MapLike<JsonElement>): DataResult<JsonElement> {
        if (map !is JsonObject && map != this.empty()) {
            return DataResult.error({ "mergeToMap called with not a map: $map" }, map)
        }

        val entries = LinkedHashMap<String, JsonElement>()
        if (map != this.empty()) {
            entries.putAll(map.jsonObject)
        }

        val missed = ArrayList<JsonElement>()
        values.entries().forEach { entry ->
            val key = entry.first
            if (key !is JsonPrimitive || !key.isString) {
                missed.add(key)
            } else {
                entries[key.content] = entry.second
            }
        }

        val output = JsonObject(entries)
        if (missed.isNotEmpty()) {
            return DataResult.error({ "some keys are not strings: $missed" }, output)
        }
        return DataResult.success(output)
    }

    override fun mergeToList(list: JsonElement, value: JsonElement): DataResult<JsonElement> {
        if (list !is JsonArray && list != this.empty()) {
            return DataResult.error({ "mergeToList called with not a list: $list" }, list)
        }

        val result = ArrayList<JsonElement>()
        if (list != this.empty()) {
            result.addAll(list.jsonArray)
        }
        result.add(value)
        return DataResult.success(JsonArray(result))
    }

    override fun mergeToList(list: JsonElement, values: List<JsonElement>): DataResult<JsonElement> {
        if (list !is JsonArray && list != this.empty()) {
            return DataResult.error({ "mergeToList called with not a list: $list" }, list)
        }

        val result = ArrayList<JsonElement>()
        if (list != this.empty()) {
            result.addAll(list.jsonArray)
        }
        result.addAll(values)
        return DataResult.success(JsonArray(result))
    }

    override fun getStringValue(input: JsonElement): DataResult<String> {
        if (input is JsonPrimitive && input.isString) {
            return DataResult.success(input.content)
        }
        return DataResult.error { "Not a string: $input" }
    }

    override fun getNumberValue(input: JsonElement): DataResult<Number> {
        if (input !is JsonPrimitive || input.isString || input.booleanOrNull != null) {
            return DataResult.error { "Not a number: $input" }
        }
        return DataResult.success(NumberLimits.parseBigDecimal(input.content))
    }

    override fun getBooleanValue(input: JsonElement): DataResult<Boolean> {
        if (input is JsonPrimitive) {
            val boolean = input.booleanOrNull
            if (boolean != null) {
                return DataResult.success(boolean)
            }
        }
        return DataResult.error { "Not a boolean: $input" }
    }

    override fun toString(): String {
        return "KJSON"
    }

    private fun <U: Any> convertPrimitiveTo(outOps: DynamicOps<U>, input: JsonPrimitive): U {
        if (input == JsonNull) {
            return outOps.empty()
        }

        if (input.isString) {
            return outOps.createString(input.content)
        }
        val boolean = input.booleanOrNull
        if (boolean != null) {
            return outOps.createBoolean(boolean)
        }
        val value: BigDecimal = NumberLimits.parseBigDecimal(input.content)
        try {
            val l = value.longValueExact()
            if (l.toByte().toLong() == l) {
                return outOps.createByte(l.toByte())
            }
            if (l.toShort().toLong() == l) {
                return outOps.createShort(l.toShort())
            }
            if (l.toInt().toLong() == l) {
                return outOps.createInt(l.toInt())
            }
            return outOps.createLong(l)
        } catch (e: ArithmeticException) {
            val d = value.toDouble()
            if (d.toFloat().toDouble() == d) {
                return outOps.createFloat(d.toFloat())
            }
            return outOps.createDouble(d)
        }
    }
}