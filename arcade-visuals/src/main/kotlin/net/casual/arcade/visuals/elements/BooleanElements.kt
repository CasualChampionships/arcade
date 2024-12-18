/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.elements

public object BooleanElements {
    private val TRUE = UniversalElement.constant(true)
    private val FALSE = UniversalElement.constant(false)

    public fun of(boolean: Boolean): PlayerSpecificElement<Boolean> {
        return if (boolean) TRUE else FALSE
    }

    public fun alwaysTrue(): PlayerSpecificElement<Boolean> {
        return TRUE
    }

    public fun alwaysFalse(): PlayerSpecificElement<Boolean> {
        return FALSE
    }
}