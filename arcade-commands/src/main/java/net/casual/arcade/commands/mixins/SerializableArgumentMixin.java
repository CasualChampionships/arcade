/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.commands.type.ServerSideArgument;
import net.minecraft.commands.arguments.SerializableArgument;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SerializableArgument.class)
public interface SerializableArgumentMixin {
    @ModifyExpressionValue(
        method = "unwrapOrThrow",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/commands/arguments/SerializableArgument$BrigadierWrapper;argument()Lnet/minecraft/commands/arguments/SerializableArgument;"
        )
    )
    @SuppressWarnings("unchecked")
    private static <T> SerializableArgument<T> unwrapServerSideArguments(SerializableArgument<T> original) {
        if (original instanceof ServerSideArgument<T> argument) {
            return (SerializableArgument<T>) argument.asSerializableArgument();
        }
        return original;
    }
}
