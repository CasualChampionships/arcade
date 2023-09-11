package net.casual.arcade.utils

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap

object EnumUtils {
    fun <E: Enum<E>> enumToMap(clazz : Class<E>, mapper: (enum: E) -> String = { it.name }): BiMap<String, E> {
        val constants = clazz.enumConstants
        val enums = HashBiMap.create<String, E>(constants.size)
        for (enumeration in constants) {
            enums[mapper(enumeration)] = enumeration
        }
        return enums
    }
}