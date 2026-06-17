/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.dialog.button

import net.casual.arcade.guis.utils.dialog.DialogWidth
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.dialog.CommonButtonData
import java.util.Optional

public abstract class ButtonBuilder {
    public var label: Component = CommonComponents.EMPTY
    public var tooltip: Component? = null
    public var width: DialogWidth = DialogWidth.BUTTON_DEFAULT

    public fun label(label: Component): ButtonBuilder {
        this.label = label
        return this
    }

    public fun tooltip(tooltip: Component?): ButtonBuilder {
        this.tooltip = tooltip
        return this
    }

    public fun width(width: Int): ButtonBuilder {
        this.width = DialogWidth(width)
        return this
    }

    protected fun buildCommon(): CommonButtonData {
        return CommonButtonData(this.label, Optional.ofNullable(this.tooltip), this.width.value)
    }
}