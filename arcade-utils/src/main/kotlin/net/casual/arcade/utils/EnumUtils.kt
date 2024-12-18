/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import java.util.*

public object EnumUtils {
    public fun <E: Enum<E>> enumToMap(clazz: Class<E>, mapper: (enum: E) -> String = { it.name }): BiMap<String, E> {
        val constants = clazz.enumConstants
        val enums = HashBiMap.create<String, E>(constants.size)
        for (enumeration in constants) {
            enums[mapper(enumeration)] = enumeration
        }
        return enums
    }

    public inline fun <reified E: Enum<E>> emptySet(): EnumSet<E> {
        return EnumSet.noneOf(E::class.java)
    }

    public inline fun <reified E: Enum<E>> completeSet(): EnumSet<E> {
        return EnumSet.allOf(E::class.java)
    }

    public inline fun <reified E: Enum<E>, V> mapOf(): EnumMap<E, V> {
        return EnumMap(E::class.java)
    }

    public inline fun <reified E: Enum<E>, V> mapOf(vararg entries: Pair<E, V>): EnumMap<E, V> {
        val map = mapOf<E, V>()
        for ((key, value) in entries) {
            map[key] = value
        }
        return map
    }
}