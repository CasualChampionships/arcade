/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.dialog

import net.casual.arcade.guis.dialog.button.ActionButtonBuilder
import net.casual.arcade.guis.utils.dialog.ActionButton
import net.minecraft.server.dialog.MultiActionDialog
import java.util.Optional

public class MultiActionDialogBuilder: DialogBuilder() {
    private val actions = ArrayList<ActionButton>()

    public var exitAction: ActionButton? = null
    public var columns: Int = 2

    public fun action(action: ActionButton): MultiActionDialogBuilder {
        this.actions.add(action)
        return this
    }

    public fun action(block: ActionButtonBuilder.() -> Unit): MultiActionDialogBuilder {
        return this.action(ActionButton(block))
    }

    public fun exitAction(exitAction: ActionButton): MultiActionDialogBuilder {
        this.exitAction = exitAction
        return this
    }

    public fun exitAction(block: ActionButtonBuilder.() -> Unit): MultiActionDialogBuilder {
        return this.exitAction(ActionButton(block))
    }

    public fun columns(columns: Int): MultiActionDialogBuilder {
        this.columns = columns
        return this
    }

    override fun build(): MultiActionDialog {
        return MultiActionDialog(this.buildCommon(), this.actions, Optional.ofNullable(this.exitAction), this.columns)
    }
}