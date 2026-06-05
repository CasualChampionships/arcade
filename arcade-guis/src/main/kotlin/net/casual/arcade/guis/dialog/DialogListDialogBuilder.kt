/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.dialog

import net.casual.arcade.guis.dialog.button.ActionButtonBuilder
import net.casual.arcade.guis.utils.dialog.ActionButton
import net.casual.arcade.guis.utils.dialog.DialogWidth
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.server.dialog.Dialog
import net.minecraft.server.dialog.DialogListDialog
import java.util.Optional

public class DialogListDialogBuilder: DialogBuilder() {
    private var dialogs = ArrayList<HolderSet<Dialog>>()

    public var exitAction: ActionButton? = null
    public var columns: Int = 2
    public var buttonWidth: DialogWidth = DialogWidth.BUTTON_DEFAULT

    public fun dialogs(dialogs: HolderSet<Dialog>): DialogListDialogBuilder {
        this.dialogs.add(dialogs)
        return this
    }

    public fun dialog(dialog: Holder<Dialog>): DialogListDialogBuilder {
        return this.dialogs(HolderSet.direct(dialog))
    }

    public fun dialog(dialog: Dialog): DialogListDialogBuilder {
        return this.dialogs(HolderSet.direct(Holder.direct(dialog)))
    }

    public fun exitAction(exitAction: ActionButton?): DialogListDialogBuilder {
        this.exitAction = exitAction
        return this
    }

    public fun exitAction(block: ActionButtonBuilder.() -> Unit): DialogListDialogBuilder {
        return this.exitAction(ActionButton(block))
    }

    public fun columns(columns: Int): DialogListDialogBuilder {
        this.columns = columns
        return this
    }

    public fun buttonWidth(buttonWidth: Int): DialogListDialogBuilder {
        this.buttonWidth = DialogWidth(buttonWidth)
        return this
    }

    override fun build(): DialogListDialog {
        val dialogs = when {
            this.dialogs.isEmpty() -> HolderSet.empty()
            this.dialogs.size == 1 -> this.dialogs[0]
            else -> HolderSet.direct(this.dialogs.flatten())
        }
        return DialogListDialog(this.buildCommon(), dialogs, Optional.ofNullable(this.exitAction), this.columns, this.buttonWidth.value)
    }
}