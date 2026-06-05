/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.dialog.input

import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.dialog.input.BooleanInput

public class BooleanInputBuilder: InputBuilder() {
    public var label: Component = CommonComponents.EMPTY
    public var initial: Boolean = false
    public var onTrue: String = ""
    public var onFalse: String = ""

    public fun label(label: Component): BooleanInputBuilder {
        this.label = label
        return this
    }

    public fun initial(initial: Boolean): BooleanInputBuilder {
        this.initial = initial
        return this
    }

    public fun onTrue(onTrue: String): BooleanInputBuilder {
        this.onTrue = onTrue
        return this
    }

    public fun onFalse(onFalse: String): BooleanInputBuilder {
        this.onFalse = onFalse
        return this
    }

    override fun build(): BooleanInput {
        return BooleanInput(this.label, this.initial, this.onTrue, this.onFalse)
    }
}