/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.casual.arcade.commands.requirements.PlayerCommandRequirement;
import net.minecraft.commands.ParseBox;
import net.minecraft.commands.ParseContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @WrapOperation(
        method = "parseCommand",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/commands/ParseBox;parse(Ljava/lang/String;Lnet/minecraft/commands/ParseContext;)Lnet/minecraft/commands/ParseBox$Result;"
        )
    )
    private ParseBox.Result xyz(
        ParseBox instance,
        String commandString,
        ParseContext context,
        Operation<ParseBox.Result> original
    ) {
        return ScopedValue.where(PlayerCommandRequirement.PLAYER_CONTEXT, this.player)
            .call(() -> original.call(instance, commandString, context));
    }
}
