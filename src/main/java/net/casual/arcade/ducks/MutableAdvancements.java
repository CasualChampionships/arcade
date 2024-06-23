package net.casual.arcade.ducks;

import net.minecraft.advancements.AdvancementHolder;

import java.util.Collection;

public interface MutableAdvancements {
	void arcade$addAllAdvancements(Collection<AdvancementHolder> advancements);

	void arcade$addAdvancement(AdvancementHolder advancement);

	void arcade$removeAdvancement(AdvancementHolder advancement);
}
