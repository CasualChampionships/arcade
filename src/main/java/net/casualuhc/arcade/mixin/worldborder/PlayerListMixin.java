package net.casualuhc.arcade.mixin.worldborder;

import net.casualuhc.arcade.utils.BorderUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@Redirect(
		method = "addWorldborderListener",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/border/WorldBorder;addListener(Lnet/minecraft/world/level/border/BorderChangeListener;)V"
		)
	)
	private void onAddListener(WorldBorder instance, BorderChangeListener listener, ServerLevel level) {
		BorderUtils.addOriginalListener(level, listener);
	}
}
