package net.casual.arcade.mixin.extensions;

import net.casual.arcade.ducks.Arcade$ExtensionHolder;
import net.casual.arcade.ducks.Arcade$ExtensionDataHolder;
import net.casual.arcade.extensions.ExtensionHolder;
import net.casual.arcade.extensions.ExtensionMap;
import net.casual.arcade.utils.ExtensionUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements Arcade$ExtensionHolder, Arcade$ExtensionDataHolder {
	@Shadow public ServerGamePacketListenerImpl connection;

	@Unique private CompoundTag arcade$data;

	@Inject(
		method = "readAdditionalSaveData",
		at = @At("TAIL")
	)
	private void onLoadPlayer(CompoundTag compound, CallbackInfo ci) {
		this.arcade$data = compound.getCompound("arcade");
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
		return ((ExtensionHolder) this.connection).getExtensionMap();
	}

	@Override
	public void arcade$deserializeExtensionData() {
		ExtensionUtils.deserialize(this, this.arcade$data);
		this.arcade$data = null;
	}
}
