/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins.commands;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.ServerRegisterCommandEvent;
import net.casual.arcade.events.server.player.PlayerSendCommandsEvent;
import net.minecraft.commands.*;
import net.minecraft.commands.suggestions.SuggestionContext;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(Commands.class)
public abstract class CommandsMixin {
    @Inject(
		method = "registerVanillaCommands",
		at = @At("TAIL")
	)
	private static void broadcastRegisterVanillaCommands(
		CommandDispatcher<CommandSourceStack> dispatcher,
		Commands.CommandSelection commandSelection,
		CommandBuildContext context,
		CallbackInfo ci
	) {
		ServerRegisterCommandEvent event = new ServerRegisterCommandEvent(dispatcher, context);
		GlobalEventHandler.Server.broadcast(event);
	}

	@ModifyExpressionValue(
		method = "sendCommands",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/commands/SuggestionBox;toPacket(Lnet/minecraft/server/level/ServerPlayer;)Lnet/minecraft/network/protocol/game/ClientboundCommandsPacket;"
		)
	)
	private ClientboundCommandsPacket onSendCommands(
		ClientboundCommandsPacket packet,
		@Local(name = "player", argsOnly = true) ServerPlayer player
	) {
		PlayerSendCommandsEvent event = new PlayerSendCommandsEvent(player);
		GlobalEventHandler.Server.broadcast(event);
		if (event.getCustomCommandNodes().isEmpty()) {
			return packet;
		}

		// Mirroring SuggestionBox#toPacket
		ServerPlayerSuggestionContext context = new ServerPlayerSuggestionContext(player);
		Map<CommandNode<SuggestionContext>, CommandNode<SuggestionContext>> converted = new HashMap<>();
		RootCommandNode<SuggestionContext> original = packet.getRoot(SuggestionBoxAccessor.arcade_getCommandNodeBuilder());
		RootCommandNode<SuggestionContext> root = new RootCommandNode<>();
		converted.put(original, root);
		SuggestionBoxAccessor.arcade_fillUsableCommands(original, root, context, converted);

		// Then tack on our custom nodes
		for (RootCommandNode<SuggestionContext> node : event.getCustomCommandNodes()) {
			converted.put(node, root);
			SuggestionBoxAccessor.arcade_fillUsableCommands(node, root, context, converted);
		}
		return new ClientboundCommandsPacket(root, SuggestionBoxAccessor.arcade_getCommandNodeInspector());
	}
}
