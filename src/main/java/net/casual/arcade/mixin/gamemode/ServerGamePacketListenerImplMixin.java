package net.casual.arcade.mixin.gamemode;

import net.casual.arcade.entity.player.ExtendedGameMode;
import net.casual.arcade.utils.PlayerUtils;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.casual.arcade.entity.player.ExtendedGameMode.AdventureSpectator;
import static net.casual.arcade.entity.player.ExtendedGameMode.getExtendedGameMode;

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
		if (getExtendedGameMode(this.player) == AdventureSpectator) {
			switch (packet.getAction()) {
				case DROP_ITEM, DROP_ALL_ITEMS -> {
					PlayerUtils.updateSelectedSlot(this.player);
				}
			}
		}
	}
}
