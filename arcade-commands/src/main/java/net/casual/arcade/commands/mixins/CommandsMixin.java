/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.commands.type.CustomCommandNodeInspector;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Commands.class)
public class CommandsMixin {
    @ModifyExpressionValue(
        method = "sendCommands",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/commands/Commands;COMMAND_NODE_INSPECTOR:Lnet/minecraft/network/protocol/game/ClientboundCommandsPacket$NodeInspector;",
            opcode = Opcodes.GETSTATIC
        )
    )
    private ClientboundCommandsPacket.NodeInspector<CommandSourceStack> replaceCommandNodeInspector(
        ClientboundCommandsPacket.NodeInspector<CommandSourceStack> original
    ) {
        return new CustomCommandNodeInspector(original);
    }
}
