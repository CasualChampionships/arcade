/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.dialog.button

import net.minecraft.server.dialog.ActionButton
import net.minecraft.server.dialog.action.Action
import java.util.*

public class ActionButtonBuilder: ButtonBuilder() {
    public var action: Action? = null

    public fun action(action: Action?): ActionButtonBuilder {
        this.action = action
        return this
    }

    public fun build(): ActionButton {
        return ActionButton(this.buildCommon(), Optional.ofNullable(this.action))
    }
}