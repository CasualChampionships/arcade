package net.casual.arcade.commands.arguments

import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.type.CustomStringArgumentInfo
import net.casual.arcade.utils.EnumUtils

public class EnumArgument<E: Enum<E>>(
    clazz: Class<E>
): MappedArgument<E>(EnumUtils.enumToMap(clazz, Companion::checkEnumName)) {
    public companion object {
        public inline fun <reified E: Enum<E>> enumeration(): EnumArgument<E> {
            return EnumArgument(E::class.java)
        }

        @JvmStatic
        public fun <E: Enum<E>> enumeration(clazz: Class<E>): EnumArgument<E> {
            return EnumArgument(clazz)
        }

        public inline fun <reified E: Enum<E>> getEnumeration(context: CommandContext<*>, string: String): E {
            return context.getArgument(string, E::class.java)
        }

        @JvmStatic
        public fun <E: Enum<E>> getEnumeration(context: CommandContext<*>, string: String, clazz: Class<E>): E {
            return context.getArgument(string, clazz)
        }

        private fun checkEnumName(enum: Enum<*>): String {
            if (!CustomStringArgumentInfo.isAllowedWord(enum.name)) {
                throw IllegalArgumentException("Enumeration ${enum.name} has invalid characters")
            }
            return enum.name
        }
    }
}