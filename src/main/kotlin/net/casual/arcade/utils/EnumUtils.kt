package net.casual.arcade.utils

object EnumUtils {
    fun <E: Enum<E>> enumToMap(clazz: Class<E>, mapper: (enum: E) -> String = { it.name }): Map<String, E> {
        val constants = clazz.enumConstants
        val enums = HashMap<String, E>(constants.size)
        for (enumeration in constants) {
            enums[mapper(enumeration)] = enumeration
        }
        return enums
    }
}