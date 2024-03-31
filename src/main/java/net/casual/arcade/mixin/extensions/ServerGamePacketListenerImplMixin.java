package net.casual.arcade.mixin.extensions;

import net.casual.arcade.ducks.Arcade$ExtensionDataHolder;
import net.casual.arcade.ducks.Arcade$ExtensionHolder;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerExtensionEvent;
import net.casual.arcade.extensions.ExtensionMap;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin implements Arcade$ExtensionHolder {
	@Shadow public ServerPlayer player;

	@Unique private final ExtensionMap arcade$extensionMap = new ExtensionMap();

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void onCreatePlayer(
		MinecraftServer server,
		Connection connection,
		ServerPlayer player,
		CommonListenerCookie cookie,
		CallbackInfo ci
	) {
		PlayerExtensionEvent event = new PlayerExtensionEvent(player);
		GlobalEventHandler.broadcast(event);

		((Arcade$ExtensionDataHolder) player).arcade$deserializeExtensionData();
	}

	@Override
	public ExtensionMap arcade$getExtensionMap() {
		return this.arcade$extensionMap;
	}
}
