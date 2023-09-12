package net.casual.arcade.mixin.commands;

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
		if (instance.getType() instanceof CustomArgumentType custom) {
			return custom.getSuggestionProvider();
		}
		return instance.getCustomSuggestions();
	}
}
