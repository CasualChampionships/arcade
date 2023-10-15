package net.casual.arcade.mixin.extensions;

import com.mojang.authlib.GameProfile;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerCreatedEvent;
import net.casual.arcade.extensions.ExtensionMap;
import net.casual.arcade.ducks.Arcade$ExtensionHolder;
import net.casual.arcade.utils.ExtensionUtils;
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
public class ServerPlayerMixin implements Arcade$ExtensionHolder {
	@Unique
	private final ExtensionMap arcade$extensionMap = new ExtensionMap();

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
	public ExtensionMap arcade$getExtensionMap() {
		return this.arcade$extensionMap;
	}
}
