/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.player.PlayerTeamChatEvent;
import net.casual.arcade.utils.PlayerUtils;
import net.casual.arcade.utils.chat.PlayerFormattedChat;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.commands.TeamMsgCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(TeamMsgCommand.class)
public class TeamMsgCommandMixin {
	@Inject(
		method = "sendMessage",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/List;iterator()Ljava/util/Iterator;"
		),
		cancellable = true
	)
	private static void onSendTeamMessage(
		CommandSourceStack source,
		Entity sender,
		PlayerTeam team,
		List<ServerPlayer> teammates,
		PlayerChatMessage chatMessage,
		CallbackInfo ci,
		@Local LocalRef<OutgoingChatMessage> outgoing,
		@Local(argsOnly = true) LocalRef<List<ServerPlayer>> teammatesRef
	) {
		if (!(sender instanceof ServerPlayer player)) {
			return;
		}

		PlayerTeamChatEvent event = new PlayerTeamChatEvent(player, chatMessage, teammates);
		GlobalEventHandler.Server.broadcast(event);

		if (event.isCancelled()) {
			ci.cancel();
			return;
		}

		if (!event.hasMutated()) {
			teammatesRef.set(List.copyOf(event.getReceiving()));
			return;
		}

		PlayerFormattedChat chat = event.formatted();
		if (chat.getUsername() != null) {
			Component message = Component.empty().append(chat.getPrefix())
				.append(chat.getUsername())
				.append(chat.getMessage());
			PlayerUtils.broadcast(event.getReceiving(), message);
			ci.cancel();
			return;
		}

		teammatesRef.set(List.copyOf(event.getReceiving()));
		// Technically, this doesn't include the prefix set...
		outgoing.set(new OutgoingChatMessage.Disguised(chat.getMessage()));
	}
}
