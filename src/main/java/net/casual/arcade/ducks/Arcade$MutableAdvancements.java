package net.casual.arcade.ducks;

import net.casual.arcade.utils.ducks.MutableAdvancements;
import net.minecraft.advancements.Advancement;
import org.jetbrains.annotations.NotNull;

public interface Arcade$MutableAdvancements extends MutableAdvancements {
	void arcade$addAdvancement(Advancement advancement);

	void arcade$removeAdvancement(Advancement advancement);

	@Override
	default void addAdvancement(@NotNull Advancement advancement) {
		this.arcade$addAdvancement(advancement);
	}

	@Override
	default void removeAdvancement(@NotNull Advancement advancement) {
		this.arcade$removeAdvancement(advancement);
	}
}
