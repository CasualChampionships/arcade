/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.mixins;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.RootCommandNode;
import net.casual.arcade.commands.ducks.DeletableCommand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = CommandDispatcher.class, remap = false)
public class CommandDispatcherMixin<S> implements DeletableCommand {
	@Shadow @Final private RootCommandNode<S> root;

	@Override
	public boolean arcade$delete(String name) {
		return ((DeletableCommand) this.root).arcade$delete(name);
	}
}
