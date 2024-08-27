package net.casual.arcade.mixin.gamemode;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.casual.arcade.entity.player.ExtendedGameMode.AdventureSpectator;
import static net.casual.arcade.entity.player.ExtendedGameMode.getExtendedGameMode;

@Mixin(Entity.class)
public class EntityMixin {
	@Inject(
		method = "isInvisible",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onIsInvisible(CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof ServerPlayer player) {
			if (getExtendedGameMode(player) == AdventureSpectator) {
				cir.setReturnValue(true);
			}
		}
	}
}
