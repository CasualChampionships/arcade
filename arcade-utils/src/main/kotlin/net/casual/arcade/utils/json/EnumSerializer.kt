/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.json

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.casual.arcade.utils.EnumUtils

public class EnumSerializer<E: Enum<E>>(
    private val enums: BiMap<String, E>
): JsonSerializer<E> {
    override fun serialize(value: E): JsonElement {
        return JsonPrimitive(this.enums.inverse()[value])
    }

    override fun deserialize(json: JsonElement): E {
        return this.enums[json.asString]!!
    }

    override fun type(): String {
        return "enumeration"
    }

    public companion object {
        public fun <E: Enum<E>> of(type: Class<E>): EnumSerializer<E> {
            return EnumSerializer(EnumUtils.enumToMap(type))
        }

        public fun <E: Enum<E>> of(enums: Map<String, E>): EnumSerializer<E> {
            return EnumSerializer(if (enums is BiMap) enums else HashBiMap.create(enums))
        }
    }
}