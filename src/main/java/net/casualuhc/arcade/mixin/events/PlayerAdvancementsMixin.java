package net.casualuhc.arcade.mixin.events;

import net.casualuhc.arcade.events.GlobalEventHandler;
import net.casualuhc.arcade.events.player.PlayerAdvancementEvent;
import net.minecraft.advancements.Advancement;
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
		method = "award",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/advancements/Advancement;getDisplay()Lnet/minecraft/advancements/DisplayInfo;"
		)
	)
	private DisplayInfo onAward(Advancement instance) {
		PlayerAdvancementEvent event = new PlayerAdvancementEvent(this.player, instance);
		GlobalEventHandler.broadcast(event);
		return event.getAnnounce() ? instance.getDisplay() : null;
	}
}
