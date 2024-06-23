package net.casual.arcade.mixin.advancements;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import java.util.Set;

@Mixin(PlayerAdvancements.class)
public interface PlayerAdvancementsAccessor {
	@Accessor("progress")
	Map<AdvancementHolder, AdvancementProgress> getProgress();

	@Accessor("progressChanged")
	Set<AdvancementHolder> getProgressChanged();

	@Invoker("markForVisibilityUpdate")
	void updateVisibility(AdvancementHolder advancement);
}
