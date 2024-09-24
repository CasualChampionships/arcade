package net.casual.arcade.minigame.ducks;

import org.jetbrains.annotations.Nullable;

public interface OverridableColor {
	void arcade$setColor(@Nullable Integer color);

	@Nullable Integer arcade$getColor();
}
