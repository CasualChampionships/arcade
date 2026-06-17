/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.dialog.input

import net.casual.arcade.guis.utils.dialog.DialogWidth
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.dialog.input.SingleOptionInput
import java.util.Optional

public class SingleOptionInputBuilder: InputBuilder() {
    private val entries = ArrayList<SingleOptionInput.Entry>()

    public var width: DialogWidth = DialogWidth.MESSAGE_DEFAULT
    public var label: Component = CommonComponents.EMPTY
    public var labelVisible: Boolean = true

    public fun width(width: Int): SingleOptionInputBuilder {
        this.width = DialogWidth(width)
        return this
    }

    public fun entry(id: String, display: Component? = null, initial: Boolean = false): SingleOptionInputBuilder {
        this.entries.add(SingleOptionInput.Entry(id, Optional.ofNullable(display), initial))
        return this
    }

    public fun label(label: Component): SingleOptionInputBuilder {
        this.label = label
        return this
    }

    public fun labelVisible(labelVisible: Boolean): SingleOptionInputBuilder {
        this.labelVisible = labelVisible
        return this
    }

    override fun build(): SingleOptionInput {
        return SingleOptionInput(this.width.value, this.entries, this.label, this.labelVisible)
    }
}