/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.utils.dialog

import net.minecraft.server.dialog.CommonButtonData

@JvmInline
public value class DialogWidth(public val value: Int) {
    init {
        require(this.value in 1..1024) { "Dialog with must be in range 1..1024" }
    }

    public companion object {
        public val BUTTON_DEFAULT: DialogWidth = DialogWidth(CommonButtonData.DEFAULT_WIDTH)
    }
}

@JvmInline
public value class ItemBodyDimension(public val value: Int) {
    init {
        require(this.value in 1..256) { "ItemBody dimension must be in range 1..256" }
    }

    public companion object {
        public val DEFAULT: ItemBodyDimension = ItemBodyDimension(16)
    }
}
