/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.brigadier.suggestion.Suggestions;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.player.PlayerCommandSuggestionsEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommandSuggestionsProvider;
import net.minecraft.util.Util;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Mixin(ServerCommandSuggestionsProvider.class)
public class ServerCommandSuggestionProviderMixin {
    @Shadow
    @Final
    private ServerPlayer player;

    @ModifyExpressionValue(
        method = "tryProcessRequest",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/server/network/ServerCommandSuggestionsProvider$Request;command:Ljava/lang/String;",
            opcode = Opcodes.GETFIELD
        )
    )
    private String storeRequestedCommand(String original, @Share("command") LocalRef<String> command) {
        command.set(original);
        return original;
    }

    @Redirect(
        method = "tryProcessRequest",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/concurrent/CompletableFuture;thenAccept(Ljava/util/function/Consumer;)Ljava/util/concurrent/CompletableFuture;",
            remap = false
        )
    )
    private CompletableFuture<?> onCustomCommandSuggestions(
        CompletableFuture<Suggestions> vanillaSuggestions,
        Consumer<? super Suggestions> action,
        @Share("command") LocalRef<String> command
    ) {
        PlayerCommandSuggestionsEvent event = new PlayerCommandSuggestionsEvent(this.player, command.get());
        event.addSuggestions(vanillaSuggestions);
        GlobalEventHandler.Server.broadcast(event);

        List<CompletableFuture<Suggestions>> all = event.getAllSuggestions();
        return Util.sequenceFailFast(all).thenAccept(suggestions -> {
            action.accept(Suggestions.merge(command.get(), suggestions));
        });
    }
}
