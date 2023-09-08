package net.casual.arcade.mixin.commands;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.ServerRegisterCommandArgumentEvent;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ArgumentTypeInfos.class)
public abstract class ArgumentTypeInfosMixin {
	@Shadow @Final private static Map<Class<?>, ArgumentTypeInfo<?, ?>> BY_CLASS;

	@Inject(
		method = "bootstrap",
		at = @At("HEAD")
	)
	private static void onRegister(Registry<ArgumentTypeInfo<?, ?>> registry, CallbackInfoReturnable<ArgumentTypeInfo<?, ?>> cir) {
		ServerRegisterCommandArgumentEvent event = new ServerRegisterCommandArgumentEvent(BY_CLASS);
		GlobalEventHandler.broadcast(event);
	}
}
