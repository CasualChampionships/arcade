/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.serialization.json

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mojang.serialization.*
import net.casual.arcade.utils.asMutable
import net.casual.arcade.utils.serialization.EmptyValueInput
import net.minecraft.core.HolderLookup
import net.minecraft.core.RegistryAccess
import net.minecraft.util.ProblemReporter
import net.minecraft.util.ProblemReporter.FieldPathElement
import net.minecraft.util.ProblemReporter.IndexedFieldPathElement
import net.minecraft.world.level.storage.ValueInput
import java.util.*
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull
import kotlin.streams.asStream

public class JsonValueInput private constructor(
    private val reporter: ProblemReporter,
    private val empty: EmptyValueInput,
    private val ops: DynamicOps<JsonElement>,
    private val input: JsonObject
): ValueInput {
    override fun <T: Any> read(key: String, codec: Codec<T>): Optional<T> {
        val element = this.input.get(key) ?: return Optional.empty()
        return when (val result = codec.parse(this.ops, element)) {
            is DataResult.Success -> Optional.of(result.value)
            is DataResult.Error -> {
                this.reporter.report(DecodeFromFieldFailedProblem(key, element, result))
                result.partialValue
            }
        }
    }

    @Deprecated("Values should be stored with keys instead")
    override fun <T: Any> read(codec: MapCodec<T>): Optional<T> {
        val result = this.ops.getMap(this.input).flatMap { ml -> codec.decode(this.ops, ml) }
        return when (result) {
            is DataResult.Success -> Optional.of(result.value)
            is DataResult.Error -> {
                this.reporter.report(DecodeFromMapFailedProblem(result))
                result.partialValue
            }
        }
    }

    override fun child(key: String): Optional<ValueInput> {
        val child = this.getTypedElement<JsonObject>(key) ?: return Optional.empty()
        return Optional.of(this.wrapChild(key, child))
    }

    override fun childOrEmpty(key: String): ValueInput {
        val child = this.getTypedElement<JsonObject>(key) ?: return this.empty
        return this.wrapChild(key, child)
    }

    override fun childrenList(key: String): Optional<ValueInput.ValueInputList> {
        val child = this.getTypedElement<JsonArray>(key) ?: return Optional.empty()
        return Optional.of(this.wrapList(key, child))
    }

    override fun childrenListOrEmpty(key: String): ValueInput.ValueInputList {
        val child = this.getTypedElement<JsonArray>(key) ?: return this.empty.emptyChildrenList()
        return this.wrapList(key, child)
    }

    override fun <T: Any> list(key: String, elementCodec: Codec<T>): Optional<ValueInput.TypedInputList<T>> {
        val child = this.getTypedElement<JsonArray>(key) ?: return Optional.empty()
        return Optional.of(this.wrapTypedList(key, child, elementCodec))
    }

    override fun <T: Any> listOrEmpty(key: String, elementCodec: Codec<T>): ValueInput.TypedInputList<T> {
        val child = this.getTypedElement<JsonArray>(key) ?: return this.empty.emptyList()
        return this.wrapTypedList(key, child, elementCodec)
    }

    override fun getBooleanOr(key: String, defaultValue: Boolean): Boolean {
        val element = this.getTypedElement<JsonPrimitive>(key) ?: return defaultValue
        if (element.isBoolean) {
            return element.asBoolean
        }
        if (element.isNumber) {
            return element.asInt != 0
        }
        return defaultValue
    }

    override fun getByteOr(key: String, defaultValue: Byte): Byte {
        val number = this.getNumericElement(key) ?: return defaultValue
        return number.toByte()
    }

    override fun getShortOr(key: String, defaultValue: Short): Int {
        val number = this.getNumericElement(key) ?: return defaultValue.toInt()
        return number.toInt()
    }

    override fun getInt(key: String): Optional<Int> {
        val number = this.getNumericElement(key) ?: return Optional.empty()
        return Optional.of(number.toInt())
    }

    override fun getIntOr(key: String, defaultValue: Int): Int {
        val number = this.getNumericElement(key) ?: return defaultValue
        return number.toInt()
    }

    override fun getLongOr(key: String, defaultValue: Long): Long {
        val number = this.getNumericElement(key) ?: return defaultValue
        return number.toLong()
    }

    override fun getLong(key: String): Optional<Long> {
        val number = this.getNumericElement(key) ?: return Optional.empty()
        return Optional.of(number.toLong())
    }

    override fun getFloatOr(key: String, defaultValue: Float): Float {
        val number = this.getNumericElement(key) ?: return defaultValue
        return number.toFloat()
    }

    override fun getDoubleOr(key: String, defaultValue: Double): Double {
        val number = this.getNumericElement(key) ?: return defaultValue
        return number.toDouble()
    }

    override fun getString(key: String): Optional<String> {
        val element = this.getTypedElement<JsonPrimitive>(key) ?: return Optional.empty()
        return Optional.of(element.asString)
    }

    override fun getStringOr(key: String, defaultValue: String): String {
        val element = this.getTypedElement<JsonPrimitive>(key) ?: return defaultValue
        return element.asString
    }

    override fun getIntArray(key: String): Optional<IntArray> {
        return this.list(key, Codec.INT).map { l -> l.stream().mapToInt { it }.toArray() }
    }

    @Deprecated("You should avoid directly using lookups, this may be empty")
    override fun lookup(): HolderLookup.Provider {
        @Suppress("DEPRECATION")
        return this.empty.lookup()
    }

    private fun wrapChild(key: String, child: JsonObject): ValueInput {
        return wrapChild(this.empty, this.ops, child) { this.reporter.forChild(FieldPathElement(key)) }
    }

    private fun wrapList(key: String, child: JsonArray): ValueInput.ValueInputList {
        return when {
            child.isEmpty -> this.empty.emptyChildrenList()
            else -> ListWrapper(key, this.reporter, this.empty, this.ops, child)
        }
    }

    private fun <T: Any> wrapTypedList(key: String, child: JsonArray, codec: Codec<T>): ValueInput.TypedInputList<T> {
        return when {
            child.isEmpty -> this.empty.emptyList()
            else -> TypedListWrapper(key, this.reporter, this.ops, child, codec)
        }
    }

    private fun getNumericElement(key: String): Number? {
        val element = this.input.get(key) ?: return null
        if (element !is JsonPrimitive || !element.isNumber) {
            this.reporter.report(UnexpectedTypeProblem(key, Number::class.java, element::class.java))
        }
        return element.asNumber
    }

    private inline fun <reified T: JsonElement> getTypedElement(key: String): T? {
        val element = this.input.get(key) ?: return null
        if (element !is T) {
            this.reporter.report(UnexpectedTypeProblem(key, T::class.java, element::class.java))
            return null
        }
        return element
    }

    public data class DecodeFromFieldFailedProblem(
        val name: String,
        val element: JsonElement,
        val error: DataResult.Error<*>
    ): ProblemReporter.Problem {
        override fun description(): String {
            return "Failed to decode value '${this.element}' from field '${this.name}': ${this.error.message()}"
        }
    }

    public data class DecodeFromListFailedProblem(
        val name: String,
        val index: Int,
        val element: JsonElement,
        val error: DataResult.Error<*>
    ): ProblemReporter.Problem {
        override fun description(): String {
            return "Failed to decode value '${this.element}' from field '${this.name}' at index '${this.index}': ${this.error.message()}"
        }
    }

    public data class DecodeFromMapFailedProblem(val error: DataResult.Error<*>): ProblemReporter.Problem {
        override fun description(): String {
            return "Failed to decode from map: ${this.error.message()}"
        }
    }

    public data class UnexpectedTypeProblem(
        val name: String,
        val expected: Class<*>,
        val actual: Class<*>
    ): ProblemReporter.Problem {
        override fun description(): String {
            return "Expected field '${this.name}' to contain value of type ${this.expected.simpleName}, but got ${this.actual.simpleName}"
        }
    }

    public data class UnexpectedListElementTypeProblem(
        val name: String,
        val index: Int,
        val expected: Class<*>,
        val actual: Class<*>
    ): ProblemReporter.Problem {
        override fun description(): String {
            return "Expected list '${this.name}' to contain at index ${this.index} value of type ${this.expected.simpleName}, but got ${this.actual.simpleName}"
        }

    }

    private class ListWrapper(
        val name: String,
        val reporter: ProblemReporter,
        val empty: EmptyValueInput,
        val ops: DynamicOps<JsonElement>,
        val list: JsonArray
    ): ValueInput.ValueInputList {
        override fun iterator(): MutableIterator<ValueInput> {
            return this.sequence().iterator().asMutable()
        }

        override fun isEmpty(): Boolean {
            return this.list.isEmpty
        }

        override fun stream(): Stream<ValueInput> {
            return this.sequence().asStream()
        }

        private fun sequence(): Sequence<ValueInput> {
            return this.list.asSequence().mapIndexedNotNull { i, element ->
                if (element is JsonObject) this.wrapChild(i, element) else this.reportIndexUnwrapProblem(i, element)
            }
        }

        private fun wrapChild(index: Int, child: JsonObject): ValueInput {
            return wrapChild(this.empty, this.ops, child) {
                this.reporter.forChild(IndexedFieldPathElement(this.name, index))
            }
        }

        private fun reportIndexUnwrapProblem(index: Int, element: JsonElement): Nothing? {
            this.reporter.report(UnexpectedListElementTypeProblem(this.name, index, JsonObject::class.java, element::class.java))
            return null
        }
    }

    private class TypedListWrapper<T: Any>(
        val name: String,
        val reporter: ProblemReporter,
        val ops: DynamicOps<JsonElement>,
        val list: JsonArray,
        val codec: Codec<T>
    ): ValueInput.TypedInputList<T>, Iterable<T> {
        override fun iterator(): MutableIterator<T> {
            return this.sequence().iterator().asMutable()
        }

        override fun isEmpty(): Boolean {
            return this.list.isEmpty
        }

        override fun stream(): Stream<T> {
            return this.sequence().asStream()
        }

        private fun sequence(): Sequence<T> {
            return this.list.asSequence().mapIndexedNotNull { i, element ->
                when (val result = this.codec.parse(this.ops, element)) {
                    is DataResult.Success -> result.value
                    is DataResult.Error -> {
                        this.reportIndexUnwrapProblem(i, element, result)
                        result.partialValue.getOrNull()
                    }
                }
            }
        }

        private fun reportIndexUnwrapProblem(index: Int, element: JsonElement, error: DataResult.Error<*>) {
            this.reporter.report(DecodeFromListFailedProblem(this.name, index, element, error))
        }
    }

    public companion object {
        @JvmStatic
        public fun create(reporter: ProblemReporter, lookup: HolderLookup.Provider, input: JsonObject): JsonValueInput {
            return JsonValueInput(
                reporter, EmptyValueInput(lookup), lookup.createSerializationContext(JsonOps.INSTANCE), input
            )
        }

        @JvmStatic
        public fun create(reporter: ProblemReporter, input: JsonObject): JsonValueInput {
            return JsonValueInput(reporter, EmptyValueInput(RegistryAccess.EMPTY), JsonOps.INSTANCE, input)
        }

        @JvmStatic
        public fun create(reporter: ProblemReporter, input: JsonArray): ValueInput.ValueInputList {
            return ListWrapper("root", reporter, EmptyValueInput(RegistryAccess.EMPTY), JsonOps.INSTANCE, input)
        }

        @JvmStatic
        public fun create(reporter: ProblemReporter, lookup: HolderLookup.Provider, input: JsonArray): ValueInput.ValueInputList {
            return ListWrapper(
                "root", reporter, EmptyValueInput(lookup), lookup.createSerializationContext(JsonOps.INSTANCE), input
            )
        }

        private fun wrapChild(
            empty: EmptyValueInput,
            ops: DynamicOps<JsonElement>,
            child: JsonObject,
            reporter: () -> ProblemReporter
        ): ValueInput {
            return if (child.isEmpty) empty else JsonValueInput(reporter.invoke(), empty, ops, child)
        }
    }
}