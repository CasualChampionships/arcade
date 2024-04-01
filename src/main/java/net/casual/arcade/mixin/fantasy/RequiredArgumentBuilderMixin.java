package net.casual.arcade.mixin.fantasy;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.stream.Stream;

@Mixin(value = RequiredArgumentBuilder.class, remap = false)
public abstract class RequiredArgumentBuilderMixin<S, T> {
	@Shadow public abstract RequiredArgumentBuilder<S, T> suggests(SuggestionProvider<S> provider);

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void onCreate(String name, ArgumentType<T> type, CallbackInfo ci) {
		if (type instanceof DimensionArgument) {
			this.suggests((context, builder) -> {
				if (context.getSource() instanceof CommandSourceStack source) {
					Stream<ResourceLocation> resources = source.getServer().levelKeys().stream().map(ResourceKey::location);
					return SharedSuggestionProvider.suggestResource(resources, builder);
				}
				return Suggestions.empty();
			});
		}
	}
}
