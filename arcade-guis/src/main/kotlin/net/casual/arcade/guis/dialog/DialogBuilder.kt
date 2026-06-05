/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.dialog

import net.casual.arcade.guis.dialog.body.ItemBodyBuilder
import net.casual.arcade.guis.dialog.input.BooleanInputBuilder
import net.casual.arcade.guis.dialog.input.NumberRangeInputBuilder
import net.casual.arcade.guis.utils.dialog.BooleanInput
import net.casual.arcade.guis.utils.dialog.ItemBody
import net.casual.arcade.guis.utils.dialog.NumberRangeInput
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.dialog.CommonDialogData
import net.minecraft.server.dialog.Dialog
import net.minecraft.server.dialog.DialogAction
import net.minecraft.server.dialog.Input
import net.minecraft.server.dialog.body.DialogBody
import net.minecraft.server.dialog.body.PlainMessage
import net.minecraft.server.dialog.input.InputControl
import java.util.*

public abstract class DialogBuilder {
    public var title: Component = CommonComponents.EMPTY
    public var externalTitle: Component? = null
    public var canCloseWithEscape: Boolean = true
    public var pause: Boolean = false
    public var afterAction: DialogAction = DialogAction.CLOSE

    private val body = ArrayList<DialogBody>()
    private val inputs = ArrayList<Input>()

    public fun title(title: Component): DialogBuilder {
        this.title = title
        return this
    }

    public fun externalTitle(title: Component): DialogBuilder {
        this.externalTitle = title
        return this
    }

    public fun canCloseWithEscape(value: Boolean): DialogBuilder {
        this.canCloseWithEscape = value
        return this
    }

    public fun pause(pause: Boolean): DialogBuilder {
        this.pause = pause
        return this
    }

    public fun afterAction(action: DialogAction): DialogBuilder {
        this.afterAction = action
        return this
    }

    public fun addBody(body: DialogBody): DialogBuilder {
        this.body.add(body)
        return this
    }

    public fun addPlainMessageBody(contents: Component, width: Int = PlainMessage.DEFAULT_WIDTH): DialogBuilder {
        return this.addBody(PlainMessage(contents, width))
    }

    public fun addItemBody(block: ItemBodyBuilder.() -> Unit): DialogBuilder {
        return this.addBody(ItemBody(block))
    }

    public fun addInput(input: Input): DialogBuilder {
        this.inputs.add(input)
        return this
    }

    public fun addInput(key: String, input: InputControl): DialogBuilder {
        this.inputs.add(Input(key, input))
        return this
    }

    public fun addBooleanInput(key: String, block: BooleanInputBuilder.() -> Unit): DialogBuilder {
        return this.addInput(key, BooleanInput(block))
    }

    public fun addNumberRangeInput(key: String, block: NumberRangeInputBuilder.() -> Unit): DialogBuilder {
        return this.addInput(key, NumberRangeInput(block))
    }

    public abstract fun build(): Dialog

    protected fun buildCommon(): CommonDialogData {
        return CommonDialogData(
            this.title,
            Optional.ofNullable(this.externalTitle),
            this.canCloseWithEscape,
            this.pause,
            this.afterAction,
            this.body,
            this.inputs
        )
    }
}