package net.casual.arcade.mixin.bugfixes;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.arguments.EntityArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = ArgumentBuilder.class, remap = false)
public class ArgumentBuilderMixin<S, T extends ArgumentBuilder<S, T>> {
	@Unique
	private boolean arcade$askingServerForSuggestions = false;

	@ModifyReceiver(
		method = "then(Lcom/mojang/brigadier/builder/ArgumentBuilder;)Lcom/mojang/brigadier/builder/ArgumentBuilder;",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/brigadier/builder/ArgumentBuilder;build()Lcom/mojang/brigadier/tree/CommandNode;"
		)
	)
	private ArgumentBuilder<S, ?> modifyBuilder(ArgumentBuilder<S, ?> builder) {
		if (this.arcade$askingServerForSuggestions) {
			return builder;
		}
		if (builder instanceof RequiredArgumentBuilder<?, ?> argument && argument.getType() instanceof EntityArgument) {
			this.arcade$askingServerForSuggestions = true;
			argument.suggests(argument.getType()::listSuggestions);
		}
		return builder;
	}

	@ModifyArg(
		method = "then(Lcom/mojang/brigadier/tree/CommandNode;)Lcom/mojang/brigadier/builder/ArgumentBuilder;",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/brigadier/tree/RootCommandNode;addChild(Lcom/mojang/brigadier/tree/CommandNode;)V"
		)
	)
	private CommandNode<S> modifyNode(CommandNode<S> node) {
		if (this.arcade$askingServerForSuggestions) {
			return node;
		}
		if (node instanceof ArgumentCommandNode<S, ?> argument && argument.getType() instanceof EntityArgument) {
			this.arcade$askingServerForSuggestions = true;
			RequiredArgumentBuilder<S, ?> builder = argument.createBuilder();
			builder.suggests(argument.getType()::listSuggestions);
			return builder.build();
		}
		return node;
	}
}
