package net.casual.arcade.utils

object CastUtils {
    @JvmStatic
    fun <T: Any> tryCast(clazz: Class<T>, value: Any): T? {
        if (clazz.isInstance(value)) {
            @Suppress("UNCHECKED_CAST")
            return value as T
        }
        return null
    }
}