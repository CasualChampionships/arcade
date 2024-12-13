package net.casual.arcade.extensions.mixins;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.extensions.Extension;
import net.casual.arcade.extensions.ExtensionHolder;
import net.casual.arcade.extensions.ExtensionMap;
import net.casual.arcade.extensions.ducks.ExtensionDataHolder;
import net.casual.arcade.extensions.event.PlayerExtensionEvent;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin implements ExtensionHolder {
	@Unique private final ExtensionMap arcade$extensionMap = new ExtensionMap();

	@Inject(
		method = "<init>",
		at = @At("CTOR_HEAD")
	)
	private void onCreateConnectionPre(
		MinecraftServer server,
		Connection connection,
		ServerPlayer player,
		CommonListenerCookie cookie,
		CallbackInfo ci
	) {
		for (Extension extension : ((ExtensionHolder) player).getExtensionMap().all()) {
			this.arcade$extensionMap.add(extension);
		}
	}

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void onCreateConnectionPost(
		MinecraftServer server,
		Connection connection,
		ServerPlayer player,
		CommonListenerCookie cookie,
		CallbackInfo ci
	) {
		PlayerExtensionEvent event = new PlayerExtensionEvent(player);
		GlobalEventHandler.Server.broadcast(event);

		((ExtensionDataHolder) player).arcade$deserializeExtensionData();
	}

	@NotNull
	@Override
	@SuppressWarnings("AddedMixinMembersNamePattern")
	public ExtensionMap getExtensionMap() {
		return this.arcade$extensionMap;
	}
}
