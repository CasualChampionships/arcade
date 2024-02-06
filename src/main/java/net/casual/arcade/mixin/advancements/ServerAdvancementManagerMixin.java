package net.casual.arcade.mixin.advancements;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.casual.arcade.ducks.Arcade$MutableAdvancements;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.ServerAdvancementReloadEvent;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Map;

@Mixin(ServerAdvancementManager.class)
public class ServerAdvancementManagerMixin implements Arcade$MutableAdvancements {
	@Shadow private AdvancementTree tree;

	@Shadow private Map<ResourceLocation, AdvancementHolder> advancements;

	@Inject(
		method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/advancements/AdvancementTree;addAll(Ljava/util/Collection;)V"
		)
	)
	private void onReloadAdvancements(
		Map<ResourceLocation, JsonElement> object,
		ResourceManager resourceManager,
		ProfilerFiller profiler,
		CallbackInfo ci,
		@Local AdvancementTree advancementTree
	) {
		ServerAdvancementReloadEvent event = new ServerAdvancementReloadEvent((ServerAdvancementManager) (Object) this, resourceManager);
		GlobalEventHandler.broadcast(event);

		Arcade$MutableAdvancements mutable = (Arcade$MutableAdvancements) advancementTree;
		mutable.arcade$addAllAdvancements(event.getAdvancements());

		// We want to mutate advancements...
		this.advancements = new Object2ObjectOpenHashMap<>(this.advancements);
	}

	@Override
	public void arcade$addAllAdvancements(Collection<AdvancementHolder> advancements) {
		((Arcade$MutableAdvancements) this.tree).arcade$addAllAdvancements(advancements);
		for (AdvancementHolder advancement : advancements) {
			this.advancements.put(advancement.id(), advancement);
		}
	}

	@Override
	public void arcade$addAdvancement(AdvancementHolder advancement) {
		((Arcade$MutableAdvancements) this.tree).arcade$addAdvancement(advancement);
		this.advancements.put(advancement.id(), advancement);
	}

	@Override
	public void arcade$removeAdvancement(AdvancementHolder advancement) {
		((Arcade$MutableAdvancements) this.tree).arcade$removeAdvancement(advancement);
		this.advancements.remove(advancement.id());
	}
}
