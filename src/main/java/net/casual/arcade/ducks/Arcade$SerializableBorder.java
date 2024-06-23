package net.casual.arcade.ducks;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public interface Arcade$SerializableBorder {
	CompoundTag arcade$serialize();

	void arcade$deserialize(@NotNull CompoundTag compound);
}
