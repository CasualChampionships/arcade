package net.casual.arcade.commands.mixins;

import net.casual.arcade.commands.hidden.HiddenCommandManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Inject(
		method = "tickServer",
		at = @At("TAIL")
	)
	private void onTick(CallbackInfo ci) {
		HiddenCommandManager.tick();
	}
}
