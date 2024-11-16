package net.casual.arcade.events.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerAdvancementEvent;
import net.casual.arcade.events.player.PlayerSystemMessageEvent;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.network.chat.Component;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {
	@Shadow private ServerPlayer player;

	@WrapWithCondition(
		method = "award",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/advancements/AdvancementRewards;grant(Lnet/minecraft/server/level/ServerPlayer;)V"
		)
	)
	private boolean onAwardAdvancement(
		AdvancementRewards instance,
		ServerPlayer player,
		AdvancementHolder holder,
		@Share("event") LocalRef<PlayerAdvancementEvent> eventRef
	) {
		PlayerAdvancementEvent event = new PlayerAdvancementEvent(this.player, holder);
		GlobalEventHandler.broadcast(event);
		eventRef.set(event);
		return event.getReward();
	}

	@WrapWithCondition(
		method = "award",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V",
			remap = false
		)
	)
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private boolean onAnnounce(
		Optional<?> instance,
		Consumer<?> action,
		@Share("event") LocalRef<PlayerAdvancementEvent> eventRef
	) {
		return eventRef.get().getAnnounce();
	}

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
		Operation<Void> original
	) {
		PlayerSystemMessageEvent.broadcast(this.player, instance, message, bypassHiddenChat, original);
	}
}
