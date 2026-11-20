/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins.commands;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.SuggestionBox;
import net.minecraft.commands.suggestions.SuggestionContext;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.server.permissions.PermissionSetSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(SuggestionBox.class)
public interface SuggestionBoxAccessor {
    @Accessor("COMMAND_NODE_INSPECTOR")
    static ClientboundCommandsPacket.NodeInspector<PermissionSetSupplier> arcade_getCommandNodeInspector() {
        throw new AssertionError();
    }
    @Accessor("COMMAND_NODE_BUILDER")
    static ClientboundCommandsPacket.NodeBuilder<SuggestionContext> arcade_getCommandNodeBuilder() {
        throw new AssertionError();
    }

    @Invoker("fillUsableCommands")
    static <S> void arcade_fillUsableCommands(
        CommandNode<S> source,
        CommandNode<S> target,
        S commandFilter,
        Map<CommandNode<S>, CommandNode<S>> converted
    ) {
        throw new AssertionError();
    }
}
