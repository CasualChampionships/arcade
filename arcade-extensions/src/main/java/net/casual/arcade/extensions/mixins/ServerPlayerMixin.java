package net.casual.arcade.extensions.mixins;

import net.casual.arcade.extensions.ExtensionHolder;
import net.casual.arcade.extensions.ExtensionMap;
import net.casual.arcade.extensions.ducks.ExtensionDataHolder;
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
public class ServerPlayerMixin implements ExtensionHolder, ExtensionDataHolder {
	@Unique private ExtensionMap arcade$extensions;

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
		ExtensionHolder.serialize(this, arcade);
		compound.put("arcade", arcade);
	}

	@Override
	public void arcade$deserializeExtensionData() {
		if (this.arcade$data != null) {
			ExtensionHolder.deserialize(this, this.arcade$data);
			this.arcade$data = null;
			this.arcade$extensions = null;
		}
	}

	@NotNull
	@Override
	@SuppressWarnings("AddedMixinMembersNamePattern")
	public ExtensionMap getExtensionMap() {
		if (this.connection != null) {
			return ((ExtensionHolder) this.connection).getExtensionMap();
		}
		// In the case that the connection is not initialized, yet
		// we add them to this temporary map which will transfer
		// them whenever the connection is initialized
		if (this.arcade$extensions == null) {
			this.arcade$extensions = new ExtensionMap();
		}
		return this.arcade$extensions;
	}
}
