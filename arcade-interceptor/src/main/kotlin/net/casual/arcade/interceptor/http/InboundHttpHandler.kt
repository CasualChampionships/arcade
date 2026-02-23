package net.casual.arcade.interceptor.http

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.FullHttpRequest

public interface InboundHttpHandler {
    public fun isRelevant()

    public fun tryHandle(ctx: ChannelHandlerContext, request: FullHttpRequest): Boolean
}