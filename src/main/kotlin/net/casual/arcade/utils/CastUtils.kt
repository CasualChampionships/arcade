package net.casual.arcade.utils

public object CastUtils {
    @JvmStatic
    public fun <T: Any> tryCast(clazz: Class<T>, value: Any): T? {
        if (clazz.isInstance(value)) {
            @Suppress("UNCHECKED_CAST")
            return value as T
        }
        return null
    }
}