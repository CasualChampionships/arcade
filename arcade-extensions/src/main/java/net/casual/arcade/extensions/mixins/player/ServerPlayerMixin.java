/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.mixins.player;

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
		if (this.connection == null) {
			this.arcade$data = compound.getCompoundOrEmpty("arcade");
			return;
		}
		ExtensionHolder.deserialize(this, compound.getCompoundOrEmpty("arcade"));
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
