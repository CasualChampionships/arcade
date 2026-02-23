/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.interceptor

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

@Sharable
public class InboundInterceptorHandler(
    private val interceptor: InboundInterceptor
): ChannelInboundHandlerAdapter() {
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (!this.interceptor.intercept(ctx, msg)) {
            ctx.pipeline().remove(this)
            ctx.fireChannelRead(msg)
        }
    }
}