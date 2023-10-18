package net.casual.arcade.mixin.commands;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.player.PlayerSendCommandsEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(Commands.class)
public abstract class CommandsMixin {
	@Shadow protected abstract void fillUsableCommands(CommandNode<CommandSourceStack> rootCommandSource, CommandNode<SharedSuggestionProvider> rootSuggestion, CommandSourceStack source, Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> commandNodeToSuggestionNode);

	@Inject(
		method = "sendCommands",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/commands/Commands;fillUsableCommands(Lcom/mojang/brigadier/tree/CommandNode;Lcom/mojang/brigadier/tree/CommandNode;Lnet/minecraft/commands/CommandSourceStack;Ljava/util/Map;)V"
		)
	)
	private void onSendCommands(
		ServerPlayer player,
		CallbackInfo ci,
		@Local Map<CommandNode<CommandSourceStack>, CommandNode<SharedSuggestionProvider>> map,
		@Local RootCommandNode<SharedSuggestionProvider> rootCommandNode
	) {
		PlayerSendCommandsEvent event = new PlayerSendCommandsEvent(player);
		GlobalEventHandler.broadcast(event);
		for (RootCommandNode<CommandSourceStack> node : event.getCustomCommandNodes()) {
			map.put(node, rootCommandNode);
			this.fillUsableCommands(node, rootCommandNode, player.createCommandSourceStack(), map);
		}
	}
}
