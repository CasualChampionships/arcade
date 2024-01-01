package net.casual.arcade.ducks;

import net.casual.arcade.utils.ducks.MutableAdvancements;
import net.minecraft.advancements.AdvancementHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface Arcade$MutableAdvancements extends MutableAdvancements {
	void arcade$addAllAdvancements(Collection<AdvancementHolder> advancements);

	void arcade$addAdvancement(AdvancementHolder advancement);

	void arcade$removeAdvancement(AdvancementHolder advancement);

	@Override
	default void addAllAdvancements(@NotNull Collection<AdvancementHolder> advancements) {
		this.arcade$addAllAdvancements(advancements);
	}

	@Override
	default void addAdvancement(@NotNull AdvancementHolder advancement) {
		this.arcade$addAdvancement(advancement);
	}

	@Override
	default void removeAdvancement(@NotNull AdvancementHolder advancement) {
		this.arcade$removeAdvancement(advancement);
	}
}
