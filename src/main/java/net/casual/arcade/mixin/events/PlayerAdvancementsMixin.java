package net.casual.arcade.mixin.events;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerAdvancementEvent;
import net.casual.arcade.events.player.PlayerSystemMessageEvent;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {
	@Shadow private ServerPlayer player;

	@WrapOperation(
		method = "method_53637",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
		)
	)
	private void onBroadcastAnnouncement(
		PlayerList instance,
		Component message,
		boolean bypassHiddenChat,
		Operation<Void> original,
		@Local(argsOnly = true) AdvancementHolder holder
	) {
		PlayerAdvancementEvent event = new PlayerAdvancementEvent(this.player, holder);
		GlobalEventHandler.broadcast(event);
		if (!event.getAnnounce()) {
			return;
		}

		PlayerSystemMessageEvent.broadcast(this.player, instance, message, bypassHiddenChat, original);
	}
}
