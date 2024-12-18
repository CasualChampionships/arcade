/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.mixins;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import net.casual.arcade.commands.type.CustomArgumentType;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientboundCommandsPacket.class)
public class ClientboundCommandsPacketMixin {
	@Redirect(
		method = "createEntry",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/brigadier/tree/ArgumentCommandNode;getCustomSuggestions()Lcom/mojang/brigadier/suggestion/SuggestionProvider;",
			remap = false
		)
	)
	private static SuggestionProvider<?> getCustomSuggestions(ArgumentCommandNode<?, ?> instance) {
		if (instance.getType() instanceof CustomArgumentType<?> custom) {
			return custom.getSuggestionProvider();
		}
		return instance.getCustomSuggestions();
	}
}
