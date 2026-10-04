/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.animation.molang

import gg.moonflower.molangcompiler.api.bridge.MolangVariable
import kotlin.reflect.KProperty

internal operator fun MolangVariable.getValue(thisRef: Any?, prop: KProperty<*>): Float {
    return this.value
}

internal operator fun MolangVariable.setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
    this.value = value
}