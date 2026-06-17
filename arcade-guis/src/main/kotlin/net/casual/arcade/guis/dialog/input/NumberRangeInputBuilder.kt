/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.dialog.input

import net.casual.arcade.guis.utils.dialog.DialogWidth
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.dialog.body.PlainMessage
import net.minecraft.server.dialog.input.NumberRangeInput
import net.minecraft.server.dialog.input.NumberRangeInput.RangeInfo
import java.util.Optional

public class NumberRangeInputBuilder: InputBuilder() {
    public var width: DialogWidth = DialogWidth.MESSAGE_DEFAULT
    public var label: Component = CommonComponents.EMPTY
    public var labelFormat: String = "%s %s"
    public var rangeInfo: RangeInfo = RangeInfo(0.0F, 1.0F, Optional.empty(), Optional.empty())

    public fun width(width: Int): NumberRangeInputBuilder {
        this.width = DialogWidth(width)
        return this
    }

    public fun label(label: Component): NumberRangeInputBuilder {
        this.label = label
        return this
    }

    public fun labelFormat(labelFormat: String): NumberRangeInputBuilder {
        this.labelFormat = labelFormat
        return this
    }

    public fun rangeInfo(rangeInfo: RangeInfo): NumberRangeInputBuilder {
        this.rangeInfo = rangeInfo
        return this
    }

    public fun rangeInfo(start: Float, end: Float, initial: Float? = null, step: Float? = null): NumberRangeInputBuilder {
        return this.rangeInfo(RangeInfo(start, end, Optional.ofNullable(initial), Optional.ofNullable(step)))
    }

    override fun build(): NumberRangeInput {
        return NumberRangeInput(this.width.value, this.label, this.labelFormat, this.rangeInfo)
    }
}