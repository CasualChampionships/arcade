/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import net.casual.arcade.commands.type.ServerSideArgument;
import net.minecraft.commands.arguments.SerializableArgument;
import net.minecraft.commands.suggestions.SuggestionProviders;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientboundCommandsPacket.class)
public class ClientboundCommandsPacketMixin {
    @Unique private static final SuggestionProvider<?> DUMMY_SUGGESTIONS = SuggestionProviders.serverOnly(
        (_, _, _) -> Suggestions.empty()
    );

    @ModifyExpressionValue(
        method = "createEntry",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/brigadier/tree/ArgumentCommandNode;getCustomSuggestions()Lcom/mojang/brigadier/suggestion/SuggestionProvider;"
        )
    )
    @SuppressWarnings("unchecked")
    private static <S> SuggestionProvider<S> replaceServerSideSuggestionsWithDummy(
        SuggestionProvider<S> original,
        @Local(name = "arg") ArgumentCommandNode<? extends S, ?> arg
    ) {
        SerializableArgument<?> argument = SerializableArgument.castOrThrow(arg.getType()).argument();
        if (argument instanceof ServerSideArgument<?>) {
            return (SuggestionProvider<S>) DUMMY_SUGGESTIONS;
        }
        return original;
    }
}
