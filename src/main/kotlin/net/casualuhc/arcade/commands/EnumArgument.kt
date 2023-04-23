package net.casualuhc.arcade.commands

import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack

class EnumArgument<E: Enum<E>>(
    clazz: Class<E>
): MappedArgument<E>(enumToMap(clazz)) {
    companion object {
        inline fun <reified E: Enum<E>> enumeration(): EnumArgument<E> {
            return EnumArgument(E::class.java)
        }

        @JvmStatic
        fun <E: Enum<E>> enumeration(clazz: Class<E>): EnumArgument<E> {
            return EnumArgument(clazz)
        }

        inline fun <reified E: Enum<E>> getEnumeration(context: CommandContext<CommandSourceStack>, string: String): E {
            return context.getArgument(string, E::class.java)
        }

        @JvmStatic
        fun <E: Enum<E>> getEnumeration(context: CommandContext<CommandSourceStack>, string: String, clazz: Class<E>): E {
            return context.getArgument(string, clazz)
        }

        private fun <E: Enum<E>> enumToMap(clazz: Class<E>): Map<String, E> {
            val constants = clazz.enumConstants
            val enums = HashMap<String, E>(constants.size)
            for (enumeration in constants) {
                if (!CustomStringArgumentInfo.isAllowedWord(enumeration.name)) {
                    throw IllegalArgumentException("Enumeration ${enumeration.name} has invalid characters")
                }
                enums[enumeration.name] = enumeration
            }
            return enums
        }
    }
}