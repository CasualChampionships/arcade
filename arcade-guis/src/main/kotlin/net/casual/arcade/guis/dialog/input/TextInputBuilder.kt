/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.dialog.input

import net.casual.arcade.guis.utils.dialog.DialogWidth
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.dialog.input.TextInput
import java.util.*

public class TextInputBuilder: InputBuilder() {
    public var width: DialogWidth = DialogWidth.MESSAGE_DEFAULT
    public var label: Component = CommonComponents.EMPTY
    public var labelVisible: Boolean = true
    public var initial: String = ""
    public var maxLength: Int = 32
    public var multiline: TextInput.MultilineOptions? = null

    public fun width(width: DialogWidth): TextInputBuilder {
        this.width = width
        return this
    }

    public fun label(label: Component): TextInputBuilder {
        this.label = label
        return this
    }

    public fun labelVisible(labelVisible: Boolean): TextInputBuilder {
        this.labelVisible = labelVisible
        return this
    }

    public fun initial(initial: String): TextInputBuilder {
        this.initial = initial
        return this
    }

    public fun maxLength(maxLength: Int): TextInputBuilder {
        this.maxLength = maxLength
        return this
    }

    public fun multiline(multiline: TextInput.MultilineOptions?): TextInputBuilder {
        this.multiline = multiline
        return this
    }

    public fun multiline(maxLines: Int? = null, height: Int? = null): TextInputBuilder {
        return this.multiline(TextInput.MultilineOptions(Optional.ofNullable(maxLines), Optional.ofNullable(height)))
    }

    override fun build(): TextInput {
        return TextInput(this.width.value, this.label, this.labelVisible, this.initial, this.maxLength, Optional.ofNullable(this.multiline))
    }
}