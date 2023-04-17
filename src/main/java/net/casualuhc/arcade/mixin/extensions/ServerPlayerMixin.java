package net.casualuhc.arcade.mixin.extensions;

import com.mojang.authlib.GameProfile;
import net.casualuhc.arcade.events.GlobalEventHandler;
import net.casualuhc.arcade.events.player.PlayerCreatedEvent;
import net.casualuhc.arcade.extensions.ExtensionHolder;
import net.casualuhc.arcade.extensions.ExtensionMap;
import net.casualuhc.arcade.utils.ExtensionUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements ExtensionHolder {
	@Unique
	private final ExtensionMap arcade_extensionMap = new ExtensionMap();

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void onCreatePlayer(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile, CallbackInfo ci) {
		PlayerCreatedEvent event = new PlayerCreatedEvent((ServerPlayer) (Object) this);
		GlobalEventHandler.broadcast(event);
	}

	@Inject(
		method = "readAdditionalSaveData",
		at = @At("TAIL")
	)
	private void onLoadPlayer(CompoundTag compound, CallbackInfo ci) {
		CompoundTag arcade = compound.getCompound("arcade");
		ExtensionUtils.deserialize(this, arcade);
	}

	@Inject(
		method = "addAdditionalSaveData",
		at = @At("TAIL")
	)
	private void onSavePlayer(CompoundTag compound, CallbackInfo ci) {
		CompoundTag arcade = new CompoundTag();
		ExtensionUtils.serialize(this, arcade);
		compound.put("arcade", arcade);
	}

	@Unique
	@NotNull
	@Override
	public ExtensionMap getExtensionMap() {
		return this.arcade_extensionMap;
	}
}
