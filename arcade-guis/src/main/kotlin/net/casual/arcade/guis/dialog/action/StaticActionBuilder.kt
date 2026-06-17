/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.dialog.action

import net.casual.arcade.utils.arcade
import net.minecraft.core.Holder
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.ClickEvent
import net.minecraft.resources.Identifier
import net.minecraft.server.dialog.Dialog
import net.minecraft.server.dialog.action.StaticAction
import java.net.URI
import java.util.Optional

public class StaticActionBuilder: ActionBuilder() {
    public var event: ClickEvent = ClickEvent.Custom(arcade("action"), Optional.empty())

    public fun event(event: ClickEvent): StaticActionBuilder {
        this.event = event
        return this
    }

    public fun openUrl(uri: URI): StaticActionBuilder {
        return this.event(ClickEvent.OpenUrl(uri))
    }

    public fun runCommand(command: String): StaticActionBuilder {
        return this.event(ClickEvent.RunCommand(command))
    }

    public fun suggestCommand(command: String): StaticActionBuilder {
        return this.event(ClickEvent.SuggestCommand(command))
    }

    public fun showDialog(dialog: Holder<Dialog>): StaticActionBuilder {
        return this.event(ClickEvent.ShowDialog(dialog))
    }

    public fun copyToClipboard(value: String): StaticActionBuilder {
        return this.event(ClickEvent.CopyToClipboard(value))
    }

    public fun custom(id: Identifier, payload: Tag? = null): StaticActionBuilder {
        return this.event(ClickEvent.Custom(id, Optional.ofNullable(payload)))
    }

    override fun build(): StaticAction {
        return StaticAction(this.event)
    }
}