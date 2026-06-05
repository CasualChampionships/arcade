/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.utils.dialog

import net.casual.arcade.guis.dialog.ConfirmationDialogBuilder
import net.casual.arcade.guis.dialog.DialogListDialogBuilder
import net.casual.arcade.guis.dialog.MultiActionDialogBuilder
import net.casual.arcade.guis.dialog.NoticeDialogBuilder
import net.casual.arcade.guis.dialog.ServerLinksDialogBuilder
import net.casual.arcade.guis.dialog.action.CommandTemplateActionBuilder
import net.casual.arcade.guis.dialog.action.CustomAllActionBuilder
import net.casual.arcade.guis.dialog.action.StaticActionBuilder
import net.casual.arcade.guis.dialog.body.ItemBodyBuilder
import net.casual.arcade.guis.dialog.button.ActionButtonBuilder
import net.casual.arcade.guis.dialog.input.BooleanInputBuilder
import net.casual.arcade.guis.dialog.input.NumberRangeInputBuilder
import net.minecraft.server.dialog.ActionButton
import net.minecraft.server.dialog.ConfirmationDialog
import net.minecraft.server.dialog.DialogListDialog
import net.minecraft.server.dialog.MultiActionDialog
import net.minecraft.server.dialog.NoticeDialog
import net.minecraft.server.dialog.ServerLinksDialog
import net.minecraft.server.dialog.action.CommandTemplate
import net.minecraft.server.dialog.action.CustomAll
import net.minecraft.server.dialog.action.StaticAction
import net.minecraft.server.dialog.body.ItemBody
import net.minecraft.server.dialog.input.BooleanInput
import net.minecraft.server.dialog.input.NumberRangeInput

public typealias CommandTemplate = CommandTemplate

public fun CommandTemplate(block: CommandTemplateActionBuilder.() -> Unit): CommandTemplate {
    return CommandTemplateActionBuilder().also(block).build()
}

public typealias CustomAll = CustomAll

public fun CustomAll(block: CustomAllActionBuilder.() -> Unit): CustomAll {
    return CustomAllActionBuilder().also(block).build()
}

public typealias StaticAction = StaticAction

public fun StaticAction(block: StaticActionBuilder.() -> Unit): StaticAction {
    return StaticActionBuilder().also(block).build()
}

public typealias ItemBody = ItemBody

public fun ItemBody(block: ItemBodyBuilder.() -> Unit): ItemBody {
    return ItemBodyBuilder().also(block).build()
}

public typealias ActionButton = ActionButton

public fun ActionButton(block: ActionButtonBuilder.() -> Unit): ActionButton {
    return ActionButtonBuilder().also(block).build()
}

public typealias BooleanInput = BooleanInput

public fun BooleanInput(block: BooleanInputBuilder.() -> Unit): BooleanInput {
    return BooleanInputBuilder().also(block).build()
}

public typealias NumberRangeInput = NumberRangeInput

public fun NumberRangeInput(block: NumberRangeInputBuilder.() -> Unit): NumberRangeInput {
    return NumberRangeInputBuilder().also(block).build()
}

public typealias ConfirmationDialog = ConfirmationDialog

public fun ConfirmationDialog(block: ConfirmationDialogBuilder.() -> Unit): ConfirmationDialog {
    return ConfirmationDialogBuilder().also(block).build()
}

public typealias DialogListDialog = DialogListDialog

public fun DialogListDialog(block: DialogListDialogBuilder.() -> Unit): DialogListDialog {
    return DialogListDialogBuilder().also(block).build()
}

public typealias MultiActionDialog = MultiActionDialog

public fun MultiActionDialog(block: MultiActionDialogBuilder.() -> Unit): MultiActionDialog {
    return MultiActionDialogBuilder().also(block).build()
}

public typealias NoticeDialog = NoticeDialog

public fun NoticeDialog(block: NoticeDialogBuilder.() -> Unit): NoticeDialog {
    return NoticeDialogBuilder().also(block).build()
}

public typealias ServerLinksDialog = ServerLinksDialog

public fun ServerLinksDialog(block: ServerLinksDialogBuilder.() -> Unit): ServerLinksDialog {
    return ServerLinksDialogBuilder().also(block).build()
}

public object DialogUtils {
    public val DEFAULT_ACTION_BUTTON: ActionButton = ActionButtonBuilder().build()
}