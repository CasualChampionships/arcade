package net.casual.arcade.dimensions.ducks;

import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Consumer;

public interface MutableRegistry<T> extends WritableRegistry<T> {
	boolean arcade$unregister(ResourceKey<T> key);

	boolean arcade$unregister(T value);

	void arcade$modify(Consumer<MutableRegistry<T>> modifier);
}
