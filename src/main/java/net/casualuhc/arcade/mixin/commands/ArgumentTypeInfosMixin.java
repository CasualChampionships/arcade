package net.casualuhc.arcade.mixin.commands;

import net.casualuhc.arcade.commands.EnumArgument;
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
		BY_CLASS.put(EnumArgument.class, new EnumArgument.Info());
	}
}
