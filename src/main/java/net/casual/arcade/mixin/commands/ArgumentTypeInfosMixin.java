package net.casual.arcade.mixin.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.casual.arcade.commands.arguments.EnumArgument;
import net.casual.arcade.commands.arguments.MappedArgument;
import net.casual.arcade.commands.arguments.TimeArgument;
import net.casual.arcade.commands.arguments.TimeZoneArgument;
import net.casual.arcade.commands.type.CustomStringArgumentInfo;
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
		BY_CLASS.put(EnumArgument.class, new CustomStringArgumentInfo(StringArgumentType.StringType.SINGLE_WORD));
		BY_CLASS.put(MappedArgument.class, new CustomStringArgumentInfo(StringArgumentType.StringType.SINGLE_WORD));
		BY_CLASS.put(TimeArgument.class, new CustomStringArgumentInfo(StringArgumentType.StringType.QUOTABLE_PHRASE));
		BY_CLASS.put(TimeZoneArgument.class, new CustomStringArgumentInfo(StringArgumentType.StringType.QUOTABLE_PHRASE));
	}
}
