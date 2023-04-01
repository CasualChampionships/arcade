package net.casualuhc.arcade.mixin.advancements;

import com.google.gson.JsonElement;
import net.casualuhc.arcade.advancements.AdvancementHandler;
import net.casualuhc.arcade.advancements.MutableAdvancements;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ServerAdvancementManager.class)
public class ServerAdvancementManagerMixin implements MutableAdvancements {
	@Shadow private AdvancementList advancements;

	@Inject(
		method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
		at = @At("TAIL")
	)
	private void onAdvancements(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
		AdvancementHandler.forEachCustom(this::addAdvancement);
	}

	@Override
	public void addAdvancement(@NotNull Advancement advancement) {
		((MutableAdvancements) this.advancements).addAdvancement(advancement);
	}

	@Override
	public void removeAdvancement(@NotNull Advancement advancement) {
		((MutableAdvancements) this.advancements).removeAdvancement(advancement);
	}
}
