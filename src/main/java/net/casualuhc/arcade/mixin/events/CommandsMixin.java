package net.casualuhc.arcade.mixin.events;

import com.mojang.brigadier.CommandDispatcher;
import net.casualuhc.arcade.events.EventHandler;
import net.casualuhc.arcade.events.server.ServerRegisterCommandEvent;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandsMixin {
	@Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void onRegisterCommands(Commands.CommandSelection selection, CommandBuildContext context, CallbackInfo ci) {
		ServerRegisterCommandEvent event = new ServerRegisterCommandEvent(this.dispatcher, context);
		EventHandler.broadcast(event);
	}
}
