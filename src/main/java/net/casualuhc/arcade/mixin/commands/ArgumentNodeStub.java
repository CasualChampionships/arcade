package net.casualuhc.arcade.mixin.commands;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import net.casualuhc.arcade.commands.CustomArgumentType;
import net.casualuhc.arcade.commands.CustomArgumentTypeInfo;
import net.casualuhc.arcade.commands.EnumArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.network.protocol.game.ClientboundCommandsPacket$ArgumentNodeStub")
public class ArgumentNodeStub {
	@Redirect(
		method = "<init>(Lcom/mojang/brigadier/tree/ArgumentCommandNode;)V",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/brigadier/tree/ArgumentCommandNode;getCustomSuggestions()Lcom/mojang/brigadier/suggestion/SuggestionProvider;"
		)
	)
	private static SuggestionProvider<?> getCustomSuggestions(ArgumentCommandNode<?, ?> instance) {
		if (instance.getType() instanceof CustomArgumentType custom) {
			return custom.getSuggestionProvider();
		}
		return instance.getCustomSuggestions();
	}

	@Inject(
		method = "serializeCap(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/commands/synchronization/ArgumentTypeInfo;Lnet/minecraft/commands/synchronization/ArgumentTypeInfo$Template;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void onGetSuggestionProvider(
		FriendlyByteBuf buffer,
		ArgumentTypeInfo<A, T> argumentInfo,
		T argumentInfoTemplate,
		CallbackInfo ci
	) {
		if (argumentInfo instanceof CustomArgumentTypeInfo customInfo) {
			buffer.writeVarInt(customInfo.getFacadeId(ArgumentTypeInfosAccessor.getClassMap()));
			argumentInfo.serializeToNetwork(argumentInfoTemplate, buffer);
			ci.cancel();
		}
	}
}
