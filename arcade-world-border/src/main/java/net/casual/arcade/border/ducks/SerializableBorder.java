package net.casual.arcade.border.ducks;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public interface SerializableBorder {
	CompoundTag arcade$serialize();

	void arcade$deserialize(@NotNull CompoundTag compound);
}
