/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.mixins;

import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.casual.arcade.commands.ducks.DeletableCommand;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = CommandNode.class, remap = false)
public abstract class CommandNodeMixin<S> implements DeletableCommand {
	@Shadow @Final private Map<String, CommandNode<S>> children;

	@Shadow @Final private Map<String, LiteralCommandNode<S>> literals;

	@Shadow @Final private Map<String, ArgumentCommandNode<S, ?>> arguments;

	@Override
	public boolean arcade$delete(@NotNull String name) {
		this.literals.remove(name);
		this.arguments.remove(name);
		return this.children.remove(name) != null;
	}
}
