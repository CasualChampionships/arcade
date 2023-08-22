package net.casualuhc.arcade.commands

import com.mojang.brigadier.context.CommandContext
import net.casualuhc.arcade.utils.EnumUtils
import net.minecraft.commands.CommandSourceStack

class EnumArgument<E: Enum<E>>(
    clazz: Class<E>
): MappedArgument<E>(EnumUtils.enumToMap(clazz, ::checkEnumName)) {
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

        private fun checkEnumName(enum: Enum<*>): String {
            if (!CustomStringArgumentInfo.isAllowedWord(enum.name)) {
                throw IllegalArgumentException("Enumeration ${enum.name} has invalid characters")
            }
            return enum.name
        }
    }
}