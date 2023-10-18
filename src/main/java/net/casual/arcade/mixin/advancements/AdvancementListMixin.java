package net.casual.arcade.mixin.advancements;

import net.casual.arcade.ducks.Arcade$MutableAdvancements;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.Set;

@Mixin(AdvancementList.class)
public abstract class AdvancementListMixin implements Arcade$MutableAdvancements {
	@Shadow @Final private Map<ResourceLocation, Advancement> advancements;
	@Shadow @Final private Set<Advancement> roots;
	@Shadow @Final private Set<Advancement> tasks;
	@Shadow @Nullable private AdvancementList.Listener listener;

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
		}
	}

	@Override
	public void arcade$removeAdvancement(@NotNull Advancement advancement) {
		this.remove(advancement);
	}
}
