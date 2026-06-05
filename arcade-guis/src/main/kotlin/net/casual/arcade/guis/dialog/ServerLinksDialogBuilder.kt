/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.dialog

import net.casual.arcade.guis.dialog.button.ActionButtonBuilder
import net.casual.arcade.guis.utils.dialog.ActionButton
import net.casual.arcade.guis.utils.dialog.DialogWidth
import net.minecraft.server.dialog.CommonButtonData
import net.minecraft.server.dialog.ServerLinksDialog
import java.util.Optional

public class ServerLinksDialogBuilder: DialogBuilder() {
    public var exitAction: ActionButton? = null
    public var columns: Int = 2
    public var width: DialogWidth = DialogWidth.BUTTON_DEFAULT

    public fun exitAction(exitAction: ActionButton?): ServerLinksDialogBuilder {
        this.exitAction = exitAction
        return this
    }

    public fun exitAction(block: ActionButtonBuilder.() -> Unit): ServerLinksDialogBuilder {
        return this.exitAction(ActionButton(block))
    }

    public fun columns(columns: Int): ServerLinksDialogBuilder {
        this.columns = columns
        return this
    }

    public fun width(width: Int): ServerLinksDialogBuilder {
        this.width = DialogWidth(width)
        return this
    }

    override fun build(): ServerLinksDialog {
        return ServerLinksDialog(this.buildCommon(), Optional.ofNullable(this.exitAction), this.columns, this.width.value)
    }
}