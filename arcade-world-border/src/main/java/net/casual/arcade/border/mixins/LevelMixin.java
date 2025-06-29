/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.mixins;

import net.casual.arcade.border.ducks.BorderSetter;
import net.casual.arcade.border.utils.LevelSpecificBorderBroadcaster;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public class LevelMixin implements BorderSetter {
	@Shadow @Mutable @Final private WorldBorder worldBorder;

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void onConstruct(CallbackInfo ci) {
		if ((Object) this instanceof ServerLevel level) {
			this.worldBorder.addListener(new LevelSpecificBorderBroadcaster(level));
		}
	}

	@Override
	public void arcade$setBorder(WorldBorder border) {
		for (BorderChangeListener listener : ((WorldBorderAccessor) this.worldBorder).getBorderListeners()) {
			border.addListener(listener);
		}
		this.worldBorder = border;
		if ((Object) this instanceof ServerLevel level) {
			ClientboundInitializeBorderPacket packet = new ClientboundInitializeBorderPacket(border);
			level.getServer().getPlayerList().broadcastAll(packet, level.dimension());
		}
	}
}
