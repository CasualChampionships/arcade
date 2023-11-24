package net.casual.arcade.mixin.advancements;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.casual.arcade.ducks.Arcade$MutableAdvancements;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.Set;

@Mixin(AdvancementList.class)
public abstract class AdvancementListMixin implements Arcade$MutableAdvancements {
	@Shadow @Final private Map<ResourceLocation, Advancement> advancements;
	@Shadow @Final private Set<Advancement> roots;
	@Shadow @Final private Set<Advancement> tasks;
	@Shadow @Nullable private AdvancementList.Listener listener;

	@Unique private boolean arcade$suppressLogs = false;

	@Shadow protected abstract void remove(Advancement advancement);

	@Override
	public void arcade$addAdvancement(@NotNull Advancement advancement) {
		if (advancement.getParent() == null || this.advancements.containsKey(advancement.getParent().getId())) {
			this.advancements.put(advancement.getId(), advancement);
			if (advancement.getParent() == null) {
				this.roots.add(advancement);
				if (this.listener != null) {
					this.listener.onAddAdvancementRoot(advancement);
				}
			} else {
				this.tasks.add(advancement);
				if (this.listener != null) {
					this.listener.onAddAdvancementTask(advancement);
				}
			}

			if (advancement.getRoot().getDisplay() != null) {
				TreeNodePosition.run(advancement.getRoot());
			}
		}
	}

	@Override
	public void arcade$removeAdvancement(@NotNull Advancement advancement) {
		this.arcade$suppressLogs = true;
		this.remove(advancement);
		this.arcade$suppressLogs = false;
	}

	@WrapWithCondition(
		method = "remove(Lnet/minecraft/advancements/Advancement;)V",
		at = @At(
			value = "INVOKE",
			target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V",
			remap = false
		)
	)
	private boolean onInfo(Logger instance, String string, Object o) {
		return !this.arcade$suppressLogs;
	}
}
