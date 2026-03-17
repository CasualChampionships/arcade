/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.suggestion.Suggestions;
import net.casual.arcade.events.BuiltInEventPhases;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.player.*;
import net.casual.arcade.utils.player.PlayerUtilsKt;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl {
	@Unique private static final ScopedValue<PlayerLeaveEvent> PLAYER_LEAVE_CONTEXT = ScopedValue.newInstance();

	@Shadow public ServerPlayer player;

	public ServerGamePacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie cookie) {
		super(server, connection, cookie);
	}

	@WrapMethod(method = "onDisconnect")
	private void broadcastDisconnectEvents(DisconnectionDetails details, Operation<Void> original) {
		PlayerLeaveEvent event = new PlayerLeaveEvent(this.player);
		GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.PRE_PHASES);
		ScopedValue.where(PLAYER_LEAVE_CONTEXT, event).run(() -> original.call(details));
		GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.POST_PHASES);
	}

	@WrapWithCondition(
		method = "removePlayerFromWorld",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
		)
	)
	private boolean onBroadcastLeaveMessage(PlayerList instance, Component message, boolean bypassHiddenChat) {
        return PLAYER_LEAVE_CONTEXT.isBound() && PLAYER_LEAVE_CONTEXT.get().getLeaveMessageModification() != PlayerLeaveEvent.LeaveMessageModification.Hide;
    }

	@Inject(
		method = "handleClientCommand",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/server/players/PlayerList;respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
			ordinal = 1,
			shift = At.Shift.AFTER
		)
	)
	@SuppressWarnings("DiscouragedShift")
	private void onRespawn(ServerboundClientCommandPacket packet, CallbackInfo ci) {
		PlayerRespawnEvent event = new PlayerRespawnEvent(this.player);
		GlobalEventHandler.Server.broadcast(event);
	}

	@WrapOperation(
		method = "removePlayerFromWorld",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
		)
	)
	private void onBroadcastLeaveMessage(
		PlayerList instance,
		Component message,
		boolean bypassHiddenChat,
		Operation<Void> original
	) {
		PlayerSystemMessageEvent.broadcast(this.player, instance, message, bypassHiddenChat, original);
	}

	@WrapOperation(
		method = "handleContainerClick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;clicked(IILnet/minecraft/world/inventory/ContainerInput;Lnet/minecraft/world/entity/player/Player;)V"
		)
	)
	private void onSlotClicked(
		AbstractContainerMenu instance,
		int slotId,
		int button,
		ContainerInput action,
		Player player,
		Operation<Void> original
	) {
		PlayerSlotClickEvent event = new PlayerSlotClickEvent(this.player, instance, slotId, button, action);
		GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.PRE_PHASES);
		if (event.isCancelled()) {
			return;
		}
		original.call(instance, slotId, button, action, player);
		GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.POST_PHASES);
	}

    @WrapWithCondition(
        method = "handlePlayerAction",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;drop(Z)V"
        )
    )
    private boolean onPlayerAction(ServerPlayer instance, boolean all) {
        PlayerDropItemEvent event = new PlayerDropItemEvent(instance, all);
        GlobalEventHandler.Server.broadcast(event);
        if (event.isCancelled()) {
			PlayerUtilsKt.updateSelectedSlot(this.player);
            return false;
        }
        return true;
    }

    @Definition(id = "player", field = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;player:Lnet/minecraft/server/level/ServerPlayer;")
    @Definition(id = "isSpectator", method = "Lnet/minecraft/server/level/ServerPlayer;isSpectator()Z")
    @Expression("this.player.isSpectator() == 0")
    @ModifyExpressionValue(
        method = "handlePlayerAction",
        at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 1)
    )
    private boolean onSwapHands(boolean original) {
        PlayerSwapOffhandEvent event = new PlayerSwapOffhandEvent(this.player);
        GlobalEventHandler.Server.broadcast(event);
        if (event.isCancelled()) {
            PlayerUtilsKt.updateSelectedSlot(this.player);
			PlayerUtilsKt.updateOffhandSlot(this.player);
            return false;
        }
        return original;
    }

    @Inject(
        method = "handlePickItemFromBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/state/BlockState;getCloneItemStack(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Z)Lnet/minecraft/world/item/ItemStack;"
        ),
        cancellable = true
    )
    private void onPickBlock(
		ServerboundPickItemFromBlockPacket packet,
		CallbackInfo ci,
		@Local(name = "blockState") BlockState state
	) {
        PlayerPickBlockEvent event = new PlayerPickBlockEvent(this.player, packet.pos(), state);
        GlobalEventHandler.Server.broadcast(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(
        method = "handlePickItemFromEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getPickResult()Lnet/minecraft/world/item/ItemStack;"
        ),
        cancellable = true
    )
    private void onPickEntity(
		ServerboundPickItemFromEntityPacket packet,
		CallbackInfo ci,
		@Local(name = "entity") Entity entity
	) {
        PlayerPickEntityEvent event = new PlayerPickEntityEvent(this.player, entity);
        GlobalEventHandler.Server.broadcast(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @WrapWithCondition(
        method = "handleAnimate",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"
        )
    )
    private boolean onSwingHand(ServerPlayer instance, InteractionHand hand) {
        PlayerClientSwingHandEvent event = new PlayerClientSwingHandEvent(instance, hand);
        GlobalEventHandler.Server.broadcast(event);
        return !event.isCancelled();
    }

	@Inject(
		method = "handlePlayerInput",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayer;setShiftKeyDown(Z)V"
		)
	)
	private void onSetSneaking(ServerboundPlayerInputPacket packet, CallbackInfo ci) {
		PlayerSetSneakingEvent event = new PlayerSetSneakingEvent(this.player, packet.input().shift());
		GlobalEventHandler.Server.broadcast(event);
	}

	@Inject(
		method = "performSignedChatCommand",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onChatCommand(ServerboundChatCommandSignedPacket packet, LastSeenMessages lastSeenMessages, CallbackInfo ci) {
		PlayerCommandEvent event = new PlayerCommandEvent(this.player, packet.command());
		GlobalEventHandler.Server.broadcast(event);
		if (event.isCancelled()) {
			ci.cancel();
		}
	}

	@Inject(
		method = "performUnsignedChatCommand",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onChatCommand(String command, CallbackInfo ci) {
		PlayerCommandEvent event = new PlayerCommandEvent(this.player, command);
		GlobalEventHandler.Server.broadcast(event);
		if (event.isCancelled()) {
			ci.cancel();
		}
	}

	@Redirect(
		method = "handleCustomCommandSuggestions",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/concurrent/CompletableFuture;thenAccept(Ljava/util/function/Consumer;)Ljava/util/concurrent/CompletableFuture;",
			remap = false
		)
	)
	private CompletableFuture<?> onCustomCommandSuggestions(
		CompletableFuture<Suggestions> vanillaSuggestions,
		Consumer<? super Suggestions> action,
		ServerboundCommandSuggestionPacket packet
	) {
		PlayerCommandSuggestionsEvent event = new PlayerCommandSuggestionsEvent(this.player, packet.getCommand());
		event.addSuggestions(vanillaSuggestions);
		GlobalEventHandler.Server.broadcast(event);

		List<CompletableFuture<Suggestions>> all = event.getAllSuggestions();
		return Util.sequenceFailFast(all).thenAccept(suggestions -> {
			Suggestions merged = Suggestions.merge(packet.getCommand(), suggestions);
			this.connection.send(new ClientboundCommandSuggestionsPacket(packet.getId(), merged));
		});
	}

	@Inject(
		method = "handleTeleportToEntityPacket",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private void onSpectatorTeleport(ServerboundTeleportToEntityPacket packet, CallbackInfo ci) {
		UUID target = ((ServerboundTeleportToEntityPacketAccessor) packet).getUUID();
		PlayerSpectatorTeleportEvent event = new PlayerSpectatorTeleportEvent(this.player, target);
		GlobalEventHandler.Server.broadcast(event);
		if (event.isCancelled()) {
			ci.cancel();
		}
	}
}
