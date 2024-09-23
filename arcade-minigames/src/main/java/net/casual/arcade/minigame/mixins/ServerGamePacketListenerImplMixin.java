package net.casual.arcade.minigame.mixins;

import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl {
	@Shadow public ServerPlayer player;
	@Shadow @Nullable private Entity lastVehicle;

	@Shadow public abstract void ackBlockChangesUpTo(int sequence);

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
			ci.cancel();

			double x = this.player.getX();
			double y = this.player.getY();
			double z = this.player.getZ();
			float xRot = this.player.getXRot();
			float yRot = this.player.getYRot();

			double newX = packet.getX(x);
			double newY = packet.getY(y);
			double newZ = packet.getZ(z);
			float newXRot = packet.getXRot(xRot);
			float newYRot = packet.getYRot(yRot);

			boolean samePosition = x == newX && y == newY && z == newZ;
			boolean sameRotation = xRot == newXRot && yRot == newYRot;

			if (samePosition && sameRotation) {
				return;
			}

			Minigame<?> minigame = MinigameUtils.getMinigame(this.player);
			if (minigame != null && minigame.getSettings().getCanLookAroundWhenFrozen()) {
				if (packet.hasRotation()) {
					if (packet.hasPosition() && !samePosition) {
						this.sendMovePacket(x, y, z);
					}
					this.player.absMoveTo(x, y, z, newYRot, newXRot);
					this.player.yHeadRot = newYRot;
					return;
				}
			}

			this.sendMovePacket(x, y, z, yRot, xRot);
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
		method = "handlePlayerAction",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private void onHandleAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
		if (!MinigameUtils.isTicking(this.player)) {
			switch (packet.getAction()) {
				case SWAP_ITEM_WITH_OFFHAND, DROP_ITEM, DROP_ALL_ITEMS -> {
					this.player.inventoryMenu.sendAllDataToRemote();
				}
				case START_DESTROY_BLOCK, ABORT_DESTROY_BLOCK, STOP_DESTROY_BLOCK -> {
					this.ackBlockChangesUpTo(packet.getSequence());
				}
			}
			ci.cancel();
		}
	}

	@Inject(
		method = "handleInteract",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/server/level/ServerLevel;)V",
			shift = At.Shift.AFTER
		),
		cancellable = true
	)
	private void onHandleInteract(ServerboundInteractPacket packet, CallbackInfo ci) {
		if (!MinigameUtils.isTicking(this.player)) {
			ci.cancel();
		}
	}

	@Unique
	private void sendMovePacket(double x, double y, double z) {
		this.send(new ClientboundPlayerPositionPacket(x, y, z, 0, 0, RelativeMovement.ROTATION, -1));
	}

	@Unique
	private void sendMovePacket(double x, double y, double z, float yaw, float pitch) {
		this.send(new ClientboundPlayerPositionPacket(x, y, z, yaw, pitch, Set.of(), -1));
	}
}
