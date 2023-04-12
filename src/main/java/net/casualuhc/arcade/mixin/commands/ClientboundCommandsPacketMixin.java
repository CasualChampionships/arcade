package net.casualuhc.arcade.mixin.commands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import net.casualuhc.arcade.commands.EnumArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
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
			target = "Lcom/mojang/brigadier/tree/ArgumentCommandNode;getCustomSuggestions()Lcom/mojang/brigadier/suggestion/SuggestionProvider;"
		)
	)
	private static SuggestionProvider<?> getCustomSuggestions(ArgumentCommandNode<?, ?> instance) {
		if (instance.getType() instanceof EnumArgument) {
			return SuggestionProviders.ASK_SERVER;
		}
		return instance.getCustomSuggestions();
	}
}
