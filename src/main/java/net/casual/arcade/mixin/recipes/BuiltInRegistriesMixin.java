package net.casual.arcade.mixin.recipes;

import com.mojang.serialization.Lifecycle;
import net.casual.arcade.recipes.WrappedRecipeSerializer;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.Optional;

@Mixin(BuiltInRegistries.class)
public abstract class BuiltInRegistriesMixin {
	@Shadow
	private static <T, R extends WritableRegistry<T>> R internalRegister(ResourceKey<? extends Registry<T>> key, R registry, BuiltInRegistries.RegistryBootstrap<T> bootstrap, Lifecycle lifecycle) {
		return null;
	}

	@Redirect(
		method = "<clinit>",
		slice = @Slice(
			from = @At(
				value = "FIELD",
				opcode = Opcodes.GETSTATIC,
				target = "Lnet/minecraft/core/registries/Registries;RECIPE_SERIALIZER:Lnet/minecraft/resources/ResourceKey;"
			),
			to = @At("TAIL")
		),
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/core/registries/BuiltInRegistries;registerSimple(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/registries/BuiltInRegistries$RegistryBootstrap;)Lnet/minecraft/core/Registry;",
			ordinal = 0
		)
	)
	private static Registry<RecipeSerializer<?>> onRegisterSimple(ResourceKey<? extends Registry<RecipeSerializer<?>>> key, BuiltInRegistries.RegistryBootstrap<RecipeSerializer<?>> bootstrap) {
		MappedRegistry<RecipeSerializer<?>> registry = new MappedRegistry<>(key, Lifecycle.stable(), false) {
			@Nullable
			@Override
			public ResourceLocation getKey(RecipeSerializer<?> value) {
				return super.getKey(this.unwrap(value));
			}

			@NotNull
			@Override
			public Optional<ResourceKey<RecipeSerializer<?>>> getResourceKey(RecipeSerializer<?> value) {
				return super.getResourceKey(this.unwrap(value));
			}

			@Override
			public int getId(@Nullable RecipeSerializer<?> value) {
				return super.getId(this.unwrap(value));
			}

			private RecipeSerializer<?> unwrap(RecipeSerializer<?> value) {
				if (value instanceof WrappedRecipeSerializer<?> wrapped) {
					return wrapped.getOriginal();
				}
				return value;
			}
		};
		return internalRegister(key, registry, bootstrap, Lifecycle.stable());
	}
}
