/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.mixins.player;

import net.casual.arcade.extensions.ExtensionHolder;
import net.casual.arcade.extensions.ExtensionMap;
import net.casual.arcade.extensions.ducks.ExtensionDataHolder;
import net.casual.arcade.utils.ArcadeUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements ExtensionHolder, ExtensionDataHolder {
	@Unique private ExtensionMap arcade$extensions;

	@Shadow public ServerGamePacketListenerImpl connection;

	@Unique private CompoundTag arcade$data;

	@Shadow public abstract ServerLevel level();

	@Inject(
		method = "readAdditionalSaveData",
		at = @At("TAIL")
	)
	private void onLoadPlayer(ValueInput input, CallbackInfo ci) {
		if (this.connection == null) {
            this.arcade$data = input.read(ArcadeUtils.MOD_ID, CompoundTag.CODEC).orElse(null);
			return;
		}
		ExtensionHolder.deserialize(this, input.childOrEmpty(ArcadeUtils.MOD_ID));
	}

	@Override
	public void arcade$deserializeExtensionData() {
		CompoundTag data = Optional.ofNullable(this.arcade$data).orElseGet(CompoundTag::new);
		ArcadeUtils.scopedProblemReporter(reporter -> {
			ValueInput input = TagValueInput.create(reporter, this.level().registryAccess(), data);
			ExtensionHolder.deserialize(this, input);
		});
		this.arcade$data = null;
		this.arcade$extensions = null;
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
