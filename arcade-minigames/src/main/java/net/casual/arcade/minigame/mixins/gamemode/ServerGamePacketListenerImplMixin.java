/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins.gamemode;

import net.casual.arcade.minigame.gamemode.ExtendedGameMode;
import net.casual.arcade.utils.PlayerUtils;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.casual.arcade.minigame.gamemode.ExtendedGameMode.getExtendedGameMode;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
	@Shadow public ServerPlayer player;

	@Inject(
		method = "handlePlayerAction",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
			shift = At.Shift.AFTER
		)
	)
	private void onDropItem(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
		if (getExtendedGameMode(this.player) == ExtendedGameMode.AdventureSpectator) {
			switch (packet.getAction()) {
				case DROP_ITEM, DROP_ALL_ITEMS -> {
					PlayerUtils.updateSelectedSlot(this.player);
				}
			}
		}
	}

	@Inject(
		method = "getMaximumFlyingTicks",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onGetMaxFlyingTicks(Entity entity, CallbackInfoReturnable<Integer> cir) {
		if (entity.isSpectator()) {
			cir.setReturnValue(Integer.MAX_VALUE);
		}
	}
}
