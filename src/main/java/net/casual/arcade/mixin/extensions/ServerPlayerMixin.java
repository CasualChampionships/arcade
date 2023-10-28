package net.casual.arcade.mixin.extensions;

import net.casual.arcade.ducks.Arcade$ExtensionHolder;
import net.casual.arcade.ducks.Arcade$TemporaryExtensionHolder;
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
public class ServerPlayerMixin implements Arcade$ExtensionHolder, Arcade$TemporaryExtensionHolder {
	@Shadow public ServerGamePacketListenerImpl connection;

	@Unique private ExtensionMap arcade$extensions;

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
		if (this.connection == null) {
			if (this.arcade$extensions == null) {
				this.arcade$extensions = new ExtensionMap();
			}
			return this.arcade$extensions;
		}
		return ((ExtensionHolder) this.connection).getExtensionMap();
	}

	@Override
	public ExtensionMap arcade$getTemporaryExtensionMap() {
		return this.arcade$extensions;
	}

	@Override
	public void arcade$deleteTemporaryExtensionMap() {
		this.arcade$extensions = null;
	}
}
