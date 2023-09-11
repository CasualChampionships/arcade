package net.casual.arcade.utils.json

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.casual.arcade.utils.EnumUtils

class EnumSerializer<E: Enum<E>>(
    private val enums: BiMap<String, E>
): JsonSerializer<E> {
    override fun serialize(value: E): JsonElement {
        return JsonPrimitive(this.enums.inverse()[value])
    }

    override fun deserialize(json: JsonElement): E {
        return this.enums[json.asString]!!
    }

    companion object {
        fun <E: Enum<E>> of(type: Class<E>): EnumSerializer<E> {
            return EnumSerializer(EnumUtils.enumToMap(type))
        }

        fun <E: Enum<E>> of(enums: Map<String, E>): EnumSerializer<E> {
            return EnumSerializer(if (enums is BiMap) enums else HashBiMap.create(enums))
        }
    }
}