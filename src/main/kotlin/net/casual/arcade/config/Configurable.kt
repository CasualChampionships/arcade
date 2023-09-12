package net.casual.arcade.config

import kotlin.reflect.KProperty

public interface Configurable<T> {
    public operator fun setValue(any: Any, property: KProperty<*>, value: T)

    public operator fun getValue(any: Any, property: KProperty<*>): T
}