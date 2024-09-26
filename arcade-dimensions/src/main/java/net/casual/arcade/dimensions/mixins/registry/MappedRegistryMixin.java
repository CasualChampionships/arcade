package net.casual.arcade.dimensions.mixins.registry;

import com.google.common.collect.Iterators;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.casual.arcade.dimensions.ducks.MutableRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> implements MutableRegistry<T> {
	@Shadow @Final private ObjectList<Holder.Reference<T>> byId;
	@Shadow @Final private Reference2IntMap<T> toId;
	@Shadow @Final private Map<ResourceLocation, Holder.Reference<T>> byLocation;
	@Shadow @Final private Map<ResourceKey<T>, Holder.Reference<T>> byKey;
	@Shadow @Final private Map<T, Holder.Reference<T>> byValue;

	@Shadow private boolean frozen;

	@Override
	public boolean arcade$unregister(ResourceKey<T> key) {
		Holder.Reference<T> reference = this.byLocation.remove(key.location());
		if (reference == null) {
			return false;
		}
		T value = reference.value();
		this.byKey.remove(key);
		this.byValue.remove(value);
		// We don't want to remove, that'll shift all the other id's around
		this.byId.set(this.toId.removeInt(value), null);
		return true;
	}

	@Override
	public boolean arcade$unregister(T value) {
		Holder.Reference<T> reference = this.byValue.remove(value);
		if (reference == null) {
			return false;
		}
		this.byKey.remove(reference.key());
		this.byValue.remove(value);
		this.byId.set(this.toId.removeInt(value), null);
		return true;
	}

	@Override
	public void arcade$modify(Consumer<MutableRegistry<T>> modifier) {
		boolean wasFrozen = this.frozen;
		try {
			this.frozen = false;
			modifier.accept(this);
		} finally {
			this.frozen = wasFrozen;
		}
	}

	@ModifyReturnValue(
		method = "holders",
		at = @At("RETURN")
	)
	private Stream<Holder.Reference<T>> filterHolders(Stream<Holder.Reference<T>> original) {
		return original.filter(Objects::nonNull);
	}

	@ModifyArg(
		method = "iterator",
		at = @At(
			value = "INVOKE",
			target = "Lcom/google/common/collect/Iterators;transform(Ljava/util/Iterator;Lcom/google/common/base/Function;)Ljava/util/Iterator;"
		)
	)
	private <E> Iterator<E> filterNullHolders(Iterator<E> original) {
		return Iterators.filter(original, Objects::nonNull);
	}

	@ModifyArg(
		method = "getRandom",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/Util;getRandomSafe(Ljava/util/List;Lnet/minecraft/util/RandomSource;)Ljava/util/Optional;"
		)
	)
	private List<T> filterNullHolders(List<T> selections) {
		return selections.stream().filter(Objects::nonNull).toList();
	}
}
