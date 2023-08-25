package net.casual.arcade.config

import kotlin.reflect.KProperty

interface Configurable<T> {
    operator fun setValue(any: Any, property: KProperty<*>, value: T)
    operator fun getValue(any: Any, property: KProperty<*>): T
}