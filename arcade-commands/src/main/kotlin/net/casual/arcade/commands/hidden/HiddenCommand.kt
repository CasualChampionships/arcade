/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.hidden

public fun interface HiddenCommand {
    public fun run(context: HiddenCommandContext)
}