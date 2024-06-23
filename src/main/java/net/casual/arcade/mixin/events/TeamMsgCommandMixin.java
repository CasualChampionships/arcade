package net.casual.arcade.mixin.events;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerTeamChatEvent;
import net.casual.arcade.utils.PlayerUtils;
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
		GlobalEventHandler.broadcast(event);

		if (event.isCancelled()) {
			ci.cancel();
			return;
		}

		Component replacement = event.getReplacementMessage();
		if (replacement == null) {
			teammatesRef.set(List.copyOf(event.getReceiving()));
			return;
		}

		if (event.getMessagePrefix() != null) {
			Component message = Component.empty().append(event.getMessagePrefix()).append(replacement);
			PlayerUtils.broadcast(event.getReceiving(), message);
			ci.cancel();
			return;
		}

		teammatesRef.set(List.copyOf(event.getReceiving()));
		outgoing.set(new OutgoingChatMessage.Disguised(replacement));
	}
}
