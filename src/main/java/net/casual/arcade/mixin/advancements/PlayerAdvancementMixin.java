package net.casual.arcade.mixin.advancements;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.casual.arcade.minigame.Minigame;
import net.casual.arcade.utils.MinigameUtils;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementMixin {
	@Shadow private ServerPlayer player;

	@WrapWithCondition(
		method = "method_53639",
		at = @At(
			value = "INVOKE",
			target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
			remap = false
		)
	)
	private boolean onWarnNoAdvancement(Logger instance, String string, Object o1, Object o2) {
		return false;
	}

	@ModifyExpressionValue(
		method = "markForVisibilityUpdate",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/advancements/AdvancementTree;get(Lnet/minecraft/advancements/AdvancementHolder;)Lnet/minecraft/advancements/AdvancementNode;"
		)
	)
	private AdvancementNode onGetAdvancementNode(AdvancementNode original, AdvancementHolder advancement) {
		if (original == null) {
			if (this.player != null) {
				Minigame<?> minigame = MinigameUtils.getMinigame(this.player);
				if (minigame != null) {
					return minigame.getAdvancements().getNode(advancement.id());
				}
			}
		}
		return original;
	}

	@Inject(
		method = "reload",
		at = @At("TAIL")
	)
	private void onReload(ServerAdvancementManager manager, CallbackInfo ci) {
		if (this.player != null) {
			Minigame<?> minigame = MinigameUtils.getMinigame(this.player);
			if (minigame != null) {
				minigame.getAdvancements().reloadFor(this.player);
			}
		}
	}
}
