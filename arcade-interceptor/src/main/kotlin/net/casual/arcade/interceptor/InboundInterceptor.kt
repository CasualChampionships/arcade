package net.casual.arcade.interceptor

import io.netty.channel.ChannelHandlerContext

public abstract class InboundInterceptor {
    public open fun intercept(ctx: ChannelHandlerContext, msg: Any): InterceptorResult {
        return InterceptorResult.Pass
    }
}