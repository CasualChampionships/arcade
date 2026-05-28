/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.manager

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.casual.arcade.commands.CommandTree

public interface CommandRegistry<S> {
    public fun register(literal: LiteralArgumentBuilder<S>)

    public fun register(tree: CommandTree<S>)
}