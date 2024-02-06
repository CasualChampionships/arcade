package net.casual.arcade.mixin.events;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerAdvancementEvent;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerAdvancements.class)
public class PlayerAdvancementsMixin {
	@Shadow private ServerPlayer player;

	@Redirect(
		method = "method_53637",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/advancements/DisplayInfo;shouldAnnounceChat()Z"
		)
	)
	private boolean onAward(DisplayInfo instance, AdvancementHolder holder) {
		PlayerAdvancementEvent event = new PlayerAdvancementEvent(this.player, holder);
		GlobalEventHandler.broadcast(event);
		return event.getAnnounce();
	}
}
