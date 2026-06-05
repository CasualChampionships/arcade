/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.dialog

import net.casual.arcade.guis.dialog.button.ActionButtonBuilder
import net.casual.arcade.guis.utils.dialog.ActionButton
import net.casual.arcade.guis.utils.dialog.DialogUtils
import net.minecraft.server.dialog.ConfirmationDialog

public class ConfirmationDialogBuilder: DialogBuilder() {
    public var yesButton: ActionButton = DialogUtils.DEFAULT_ACTION_BUTTON
    public var noButton: ActionButton = DialogUtils.DEFAULT_ACTION_BUTTON

    public fun yesButton(yesButton: ActionButton): ConfirmationDialogBuilder {
        this.yesButton = yesButton
        return this
    }

    public fun yesButton(block: ActionButtonBuilder.() -> Unit): ConfirmationDialogBuilder {
        return this.yesButton(ActionButton(block))
    }

    public fun noButton(noButton: ActionButton): ConfirmationDialogBuilder {
        this.noButton = noButton
        return this
    }

    public fun noButton(block: ActionButtonBuilder.() -> Unit): ConfirmationDialogBuilder {
        return this.noButton(ActionButton(block))
    }

    override fun build(): ConfirmationDialog {
        return ConfirmationDialog(this.buildCommon(), this.yesButton, this.noButton)
    }
}