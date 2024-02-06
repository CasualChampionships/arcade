package net.casual.arcade.ducks;

import net.casual.arcade.border.SerializableBorder;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.NotNull;

public interface Arcade$SerializableBorder extends SerializableBorder {
	CompoundTag arcade$serialize();

	void arcade$deserialize(@NotNull CompoundTag compound);

	@NotNull
	@Override
	@NonExtendable
	default CompoundTag serialize() {
		return this.arcade$serialize();
	}

	@Override
	@NonExtendable
	default void deserialize(@NotNull CompoundTag compound) {
		this.arcade$deserialize(compound);
	}
}
