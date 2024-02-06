package net.casual.arcade.mixin.minigame;

import net.casual.arcade.utils.MinigameUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl {
	@Shadow public ServerPlayer player;

	public ServerGamePacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie cookie) {
		super(server, connection, cookie);
	}

	@Inject(
		method = "tick",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onTick(CallbackInfo ci) {
		if (!MinigameUtils.isTicking(this.player)) {
			this.keepConnectionAlive();
			ci.cancel();
		}
	}

	@Inject(
		method = "handleUseItem",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private void onUseItem(ServerboundUseItemPacket packet, CallbackInfo ci) {
		if (!MinigameUtils.isTicking(this.player)) {
			ci.cancel();
		}
	}

	@Inject(
		method = "handleUseItemOn",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private void onUseItemOn(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
		if (!MinigameUtils.isTicking(this.player)) {
			ci.cancel();
		}
	}

	@Inject(
		method = "handleMovePlayer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private void onHandleMovement(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
		if (!MinigameUtils.isTicking(this.player)) {
			ci.cancel();
		}
	}

	@Inject(
		method = "handleMoveVehicle",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private void onHandleVehicleMovement(ServerboundMoveVehiclePacket packet, CallbackInfo ci) {
		if (!MinigameUtils.isTicking(this.player)) {
			ci.cancel();
		}
	}
}
