package net.casual.arcade.mixin.bugfixes;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RequiredArgumentBuilder.class, remap = false)
public abstract class RequiredArgumentBuilderMixin<S, T> {
	@Shadow
	public abstract RequiredArgumentBuilder<S, T> suggests(SuggestionProvider<S> provider);

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void onCreate(String name, ArgumentType<T> type, CallbackInfo ci) {
		if (type instanceof EntityArgument) {
			// This forces the client to ask the server
			this.suggests(type::listSuggestions);
		}
	}
}
