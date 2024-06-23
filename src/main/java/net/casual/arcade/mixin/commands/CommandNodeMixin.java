package net.casual.arcade.mixin.commands;

import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.casual.arcade.ducks.DeletableCommand;
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
	public void arcade$delete(@NotNull String name) {
		this.children.remove(name);
		this.literals.remove(name);
		this.arguments.remove(name);
	}
}
