package net.casual.arcade.mixin.advancements;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.utils.ducks.MutableAdvancements;
import net.casual.arcade.ducks.Arcade$MutableAdvancements;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.ServerAdvancementReloadEvent;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ServerAdvancementManager.class)
public class ServerAdvancementManagerMixin implements Arcade$MutableAdvancements {
	@Shadow private AdvancementList advancements;

	@Inject(
		method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/advancements/AdvancementList;add(Ljava/util/Map;)V"
		)
	)
	private void onReloadAdvancements(
		Map<ResourceLocation, JsonElement> object,
		ResourceManager resourceManager,
		ProfilerFiller profiler,
		CallbackInfo ci,
		@Local AdvancementList advancements
	) {
		ServerAdvancementReloadEvent event = new ServerAdvancementReloadEvent((ServerAdvancementManager) (Object) this, resourceManager);
		GlobalEventHandler.broadcast(event);

		MutableAdvancements mutable = (MutableAdvancements) advancements;
		for (Advancement advancement : event.getAdvancements()) {
			mutable.addAdvancement(advancement);
		}
	}

	@Override
	public void arcade$addAdvancement(Advancement advancement) {
		((MutableAdvancements) this.advancements).addAdvancement(advancement);
	}

	@Override
	public void arcade$removeAdvancement(Advancement advancement) {
		((MutableAdvancements) this.advancements).removeAdvancement(advancement);
	}
}
