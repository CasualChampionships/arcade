package net.casual.arcade.mixin.minigame;

import net.casual.arcade.utils.MinigameUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl {
	@Shadow public ServerPlayer player;

	@Shadow public abstract void teleport(double x, double y, double z, float yaw, float pitch);

	@Shadow private @Nullable Entity lastVehicle;

	public ServerGamePacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie cookie) {
		super(server, connection, cookie);
	}

	@Inject(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;resetPosition()V"
		),
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
			target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;ackBlockChangesUpTo(I)V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private void onUseItem(ServerboundUseItemPacket packet, CallbackInfo ci) {
		if (!MinigameUtils.isTicking(this.player)) {
			this.player.inventoryMenu.sendAllDataToRemote();
			ci.cancel();
		}
	}

	@Inject(
		method = "handleUseItemOn",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;ackBlockChangesUpTo(I)V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private void onUseItemOn(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
		if (!MinigameUtils.isTicking(this.player)) {
			this.player.inventoryMenu.sendAllDataToRemote();
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
			this.teleport(
				this.player.getX(),
				this.player.getY(),
				this.player.getZ(),
				this.player.getYRot(),
				this.player.getXRot()
			);
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
			Entity entity = this.player.getRootVehicle();
			if (entity != this.player && entity.getControllingPassenger() == this.player && entity == this.lastVehicle) {
				this.send(new ClientboundMoveVehiclePacket(entity));
			}
		}
	}

	@Inject(
		method = "handleContainerClick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private void onHandleClick(ServerboundContainerClickPacket packet, CallbackInfo ci) {
		if (!MinigameUtils.isTicking(this.player)) {
			this.player.inventoryMenu.sendAllDataToRemote();
			ci.cancel();
		}
	}
}
