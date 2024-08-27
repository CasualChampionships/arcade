package net.casual.arcade.mixin.gamemode;

import net.casual.arcade.entity.player.ExtendedGameMode;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.*;
import net.casual.arcade.utils.PlayerUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {
	@Inject(
		method = "interactOn",
		at = @At("HEAD")
	)
	private void onInteractOn(
		Entity entity,
		InteractionHand hand,
		CallbackInfoReturnable<InteractionResult> cir
	) {
		if ((Object) this instanceof ServerPlayer player) {
			if (ExtendedGameMode.getExtendedGameMode(player) == ExtendedGameMode.AdventureSpectator) {
				PlayerUtils.updateSelectedSlot(player);
			}
		}
	}
}
