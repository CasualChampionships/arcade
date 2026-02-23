/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.interceptor

import io.netty.channel.ChannelHandlerContext

public abstract class InboundInterceptor {
    public open fun intercept(ctx: ChannelHandlerContext, msg: Any): Boolean {
        return false
    }
}