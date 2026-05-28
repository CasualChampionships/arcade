/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.chunk;

import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorder;
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorders;
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorders;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(
        method = "broadcastAll(Lnet/minecraft/network/protocol/Packet;)V",
        at = @At("HEAD")
    )
    private void onBroadcastAll(Packet<?> packet, CallbackInfo ci) {
        ReplayChunkRecorders.record(packet);
    }

    @Inject(
        method = "broadcastAll(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/resources/ResourceKey;)V",
        at = @At("HEAD")
    )
    private void onBroadcastAll(Packet<?> packet, ResourceKey<Level> dimension, CallbackInfo ci) {
        for (ReplayChunkRecorder recorder : ReplayChunkRecorders.recorders()) {
            if (recorder.getLevel().dimension() == dimension) {
                recorder.record(packet);
            }
        }
    }

    @Inject(
        method = "broadcast",
        at = @At("HEAD")
    )
    private void onBroadcast(
        Player except,
        double x,
        double y,
        double z,
        double range,
        ResourceKey<Level> dimension,
        Packet<?> packet,
        CallbackInfo ci
    ) {
        if (except instanceof ServerPlayer player && player.level().dimension() == dimension) {
            ReplayPlayerRecorders.record(player, packet);
        }

        ChunkPos pos = ChunkPos.containing(BlockPos.containing(x, y, z));
        for (ReplayChunkRecorder recorder : ReplayChunkRecorders.containing(dimension, pos)) {
            recorder.record(packet);
        }
    }

    @Inject(
        method = "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Ljava/util/function/Function;Z)V",
        at = @At("HEAD")
    )
    private void onBroadcastSystemMessage(
        Component message,
        Function<ServerPlayer, Component> playerMessages,
        boolean overlay,
        CallbackInfo ci
    ) {
        ReplayChunkRecorders.record(new ClientboundSystemChatPacket(message, overlay));
    }

    @Inject(
        method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V",
        at = @At("HEAD")
    )
    private void onBroadcastChatMessage(
        PlayerChatMessage message,
        Predicate<ServerPlayer> isFiltered,
        @Nullable ServerPlayer senderPlayer,
        ChatType.Bound chatType,
        CallbackInfo ci
    ) {
        if (message.isSystem()) {
            ReplayChunkRecorders.record(new ClientboundDisguisedChatPacket(
                message.decoratedContent(),
                chatType
            ));
            return;
        }
        Component content = message.unsignedContent();
        if (content == null) {
            content = Component.literal(message.signedBody().content());
        }
        ReplayChunkRecorders.record(new ClientboundSystemChatPacket(chatType.decorate(content), false));
    }
}
