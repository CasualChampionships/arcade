package net.casual.arcade.ducks;

import net.casual.arcade.utils.ducks.MutableAdvancements;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import org.jetbrains.annotations.NotNull;

public interface Arcade$MutableAdvancements extends MutableAdvancements {
	void arcade$addAdvancement(AdvancementHolder advancement);

	void arcade$removeAdvancement(AdvancementHolder advancement);

	@Override
	default void addAdvancement(@NotNull AdvancementHolder advancement) {
		this.arcade$addAdvancement(advancement);
	}

	@Override
	default void removeAdvancement(@NotNull AdvancementHolder advancement) {
		this.arcade$removeAdvancement(advancement);
	}
}
