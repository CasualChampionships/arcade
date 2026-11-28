/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.mixins;

import com.mojang.brigadier.tree.CommandNode;
import net.casual.arcade.commands.requirements.PlayerCommandRequirement;
import net.minecraft.commands.CommandBox;
import net.minecraft.server.permissions.PermissionSetSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(CommandBox.class)
public class CommandBoxMixin {
    @Inject(
        method = "canNodeRun",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/UnsupportedOperationException;<init>(Ljava/lang/String;)V"
        ),
        cancellable = true
    )
    private static void handlePlayerRequirementCheck(
        CommandNode<? extends PermissionSetSupplier> node,
        PermissionSetSupplier context,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (node.getRequirement() instanceof PlayerCommandRequirement<?> check) {
            cir.setReturnValue(check.cast().test(context));
        }
    }

    @Inject(
        method = "convertNodePermissionCheck",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/UnsupportedOperationException;<init>(Ljava/lang/String;)V"
        ),
        cancellable = true
    )
    private static void convertPlayerRequirementCheck(
        Predicate<? extends PermissionSetSupplier> original,
        CallbackInfoReturnable<Predicate<PermissionSetSupplier>> cir
    ) {
        if (original instanceof PlayerCommandRequirement<?> owner) {
            cir.setReturnValue(owner.cast());
        }
    }
}
