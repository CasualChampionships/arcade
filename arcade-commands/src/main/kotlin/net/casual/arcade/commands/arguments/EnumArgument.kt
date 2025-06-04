/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.arguments

import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.type.CustomStringArgumentInfo
import net.casual.arcade.utils.EnumUtils

public class EnumArgument<E: Enum<E>>(
    clazz: Class<E>,
    mapper: (E) -> String = Enum<E>::name
): MappedArgument<E>(EnumUtils.enumToMap(clazz) { e -> checkEnumName(e, mapper) }) {
    public companion object {
        public inline fun <reified E: Enum<E>> enumeration(
            noinline mapper: (E) -> String = Enum<E>::name
        ): EnumArgument<E> {
            return EnumArgument(E::class.java, mapper)
        }

        @JvmStatic
        @JvmOverloads
        public fun <E: Enum<E>> enumeration(
            clazz: Class<E>,
            mapper: (E) -> String = Enum<E>::name
        ): EnumArgument<E> {
            return EnumArgument(clazz, mapper)
        }

        public inline fun <reified E: Enum<E>> getEnumeration(context: CommandContext<*>, string: String): E {
            return context.getArgument(string, E::class.java)
        }

        @JvmStatic
        public fun <E: Enum<E>> getEnumeration(context: CommandContext<*>, string: String, clazz: Class<E>): E {
            return context.getArgument(string, clazz)
        }

        private fun <E> checkEnumName(enum: E, mapper: (E) -> String): String {
            val name = mapper.invoke(enum)
            if (!CustomStringArgumentInfo.isAllowedWord(name)) {
                throw IllegalArgumentException("Enumeration name $name has invalid characters")
            }
            return name
        }
    }
}