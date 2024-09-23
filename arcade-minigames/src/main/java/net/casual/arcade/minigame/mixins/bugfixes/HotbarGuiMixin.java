package net.casual.arcade.minigame.mixins.bugfixes;

import eu.pb4.sgui.api.gui.HotbarGui;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = HotbarGui.class, remap = false)
public class HotbarGuiMixin {
	@Shadow protected int selectedSlot;

	@Inject(
		method = "onSelectedSlotChange",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onSelectedSlotChangeBugfix(int slot, CallbackInfoReturnable<Boolean> cir) {
		this.selectedSlot = Mth.clamp(slot, 0, 8);
		cir.setReturnValue(true);
	}
}
