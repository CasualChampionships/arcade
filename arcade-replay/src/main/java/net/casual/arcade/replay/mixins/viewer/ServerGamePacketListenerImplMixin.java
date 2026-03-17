/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.viewer;

import io.netty.channel.ChannelFutureListener;
import net.casual.arcade.replay.ducks.ReplayViewable;
import net.casual.arcade.replay.viewer.ReplayViewer;
import net.casual.arcade.replay.viewer.ReplayViewerPackets;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.LastSeenMessagesValidator;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin extends ServerCommonPacketListenerImpl implements ReplayViewable {
    @Mutable
    @Shadow
    @Final
    private LastSeenMessagesValidator lastSeenMessages;
    @Shadow
    @Nullable
    private RemoteChatSession chatSession;

    @Shadow
    private int nextChatIndex;
    @Unique
    private ReplayViewer replay$viewer = null;

    public ServerGamePacketListenerImplMixin(MinecraftServer minecraftServer, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraftServer, connection, commonListenerCookie);
    }

    @Override
    public void arcade$startViewingReplay(ReplayViewer viewer) {
        this.replay$viewer = viewer;
    }

    @Override
    public void arcade$stopViewingReplay() {
        if (this.replay$viewer != null) {
            this.nextChatIndex = 0;
            this.lastSeenMessages = new LastSeenMessagesValidator(20);
            this.replay$viewer = null;
            // Reset chat session
            this.chatSession = null;
        }
    }

    @Override
    public void arcade$sendReplayViewerPacket(Packet<?> packet) {
        super.send(packet, null);
    }

    @Override
    public ReplayViewer arcade$getViewingReplay() {
        return this.replay$viewer;
    }

    @Override
    public void send(@NonNull Packet<?> packet, @Nullable ChannelFutureListener sendListener) {
        if (this.replay$viewer == null || ReplayViewerPackets.clientboundBypass(packet)) {
            super.send(packet, sendListener);
        }
    }
}
