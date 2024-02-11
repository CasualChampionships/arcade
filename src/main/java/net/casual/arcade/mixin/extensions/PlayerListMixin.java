package net.casual.arcade.mixin.extensions;

import net.casual.arcade.utils.NameTagUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@Inject(
		method = "respawn",
		at = @At("TAIL")
	)
	private void onRespawn(
		ServerPlayer player,
		boolean keepEverything,
		CallbackInfoReturnable<ServerPlayer> cir
	) {
		NameTagUtils.INSTANCE.getNameTags$arcade(player).respawn(cir.getReturnValue());
	}
}
