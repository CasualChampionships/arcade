/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.casual.arcade.events.server.ServerSaveEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.SaveAllCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SaveAllCommand.class)
public class SaveAllCommandMixin {
    @WrapOperation(
        method = "saveAll",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;saveEverything(ZZZ)Z"
        )
    )
    private static boolean markSaveAsManual(
        MinecraftServer server,
        boolean silent,
        boolean flush,
        boolean force,
        Operation<Boolean> original
    ) {
        return ScopedValue.where(ServerSaveEvent.SAVE_REASON, ServerSaveEvent.Reason.Manual).call(() -> {
            return original.call(server, silent, flush, force);
        });
    }
}
