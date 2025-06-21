/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.ServerRegisterCommandEvent;
import net.casual.arcade.events.server.player.PlayerSendCommandsEvent;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(Commands.class)
public abstract class CommandsMixin {
	@Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

    @Shadow
    private static <S> void fillUsableCommands(CommandNode<S> node, CommandNode<S> root, S source, Map<CommandNode<S>, CommandNode<S>> map) {
		throw new AssertionError();
	}

    @Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void onRegisterCommands(Commands.CommandSelection selection, CommandBuildContext context, CallbackInfo ci) {
		ServerRegisterCommandEvent event = new ServerRegisterCommandEvent(this.dispatcher, context);
		GlobalEventHandler.Server.broadcast(event);
	}

	@Inject(
		method = "sendCommands",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/commands/Commands;fillUsableCommands(Lcom/mojang/brigadier/tree/CommandNode;Lcom/mojang/brigadier/tree/CommandNode;Ljava/lang/Object;Ljava/util/Map;)V"
		)
	)
	private void onSendCommands(
		ServerPlayer player,
		CallbackInfo ci,
		@Local Map<CommandNode<CommandSourceStack>, CommandNode<CommandSourceStack>> map,
		@Local RootCommandNode<CommandSourceStack> root
	) {
		PlayerSendCommandsEvent event = new PlayerSendCommandsEvent(player);
		GlobalEventHandler.Server.broadcast(event);
		for (RootCommandNode<CommandSourceStack> node : event.getCustomCommandNodes()) {
			map.put(node, root);
			fillUsableCommands(node, root, player.createCommandSourceStack(), map);
		}
	}
}
