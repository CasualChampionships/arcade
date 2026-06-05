/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.dialog

import net.casual.arcade.guis.dialog.button.ActionButtonBuilder
import net.casual.arcade.guis.utils.dialog.ActionButton
import net.casual.arcade.guis.utils.dialog.DialogUtils
import net.minecraft.server.dialog.NoticeDialog

public class NoticeDialogBuilder: DialogBuilder() {
    public var action: ActionButton = DialogUtils.DEFAULT_ACTION_BUTTON

    public fun action(action: ActionButton): NoticeDialogBuilder {
        this.action = action
        return this
    }

    public fun action(block: ActionButtonBuilder.() -> Unit): NoticeDialogBuilder {
        return this.action(ActionButton(block))
    }

    override fun build(): NoticeDialog {
        return NoticeDialog(this.buildCommon(), this.action)
    }
}