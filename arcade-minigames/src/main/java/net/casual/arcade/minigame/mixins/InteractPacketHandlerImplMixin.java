/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.minigame.utils.MinigameUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.network.ServerGamePacketListenerImpl$1")
public class InteractPacketHandlerImplMixin {
	@Shadow @Final ServerGamePacketListenerImpl field_28963;

	@Inject(
		method = "onInteraction(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void canInteractAt(CallbackInfo ci) {
		Minigame minigame = MinigameUtils.getMinigame(this.field_28963.player);
		if (minigame != null && !minigame.getSettings().canInteractEntities.get(this.field_28963.player)) {
			ci.cancel();
		}
	}

	@Inject(
		method = "onInteraction(Lnet/minecraft/world/InteractionHand;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void canInteract(CallbackInfo ci) {
		Minigame minigame = MinigameUtils.getMinigame(this.field_28963.player);
		if (minigame != null && !minigame.getSettings().canInteractEntities.get(this.field_28963.player)) {
			ci.cancel();
		}
	}

	@WrapWithCondition(
		method = "onAttack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerPlayer;attack(Lnet/minecraft/world/entity/Entity;)V"
		)
	)
	private boolean canAttack(ServerPlayer player, Entity target) {
		Minigame minigame = MinigameUtils.getMinigame(player);
		return minigame == null || minigame.getSettings().canAttackEntities.get(player);
	}
}
