/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.mixins;

import com.mojang.brigadier.arguments.ArgumentType;
import net.casual.arcade.commands.type.CustomArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.network.protocol.game.ClientboundCommandsPacket$ArgumentNodeStub")
public class ArgumentNodeStubMixin {
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
		if (argumentInfo instanceof CustomArgumentTypeInfo<?> customInfo) {
			ArgumentTypeInfo<?, ?> typeInfo = ArgumentTypeInfosAccessor.getClassMap().get(customInfo.getFacadeType());
			buffer.writeVarInt(BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getId(typeInfo));
			argumentInfo.serializeToNetwork(argumentInfoTemplate, buffer);
			ci.cancel();
		}
	}
}
