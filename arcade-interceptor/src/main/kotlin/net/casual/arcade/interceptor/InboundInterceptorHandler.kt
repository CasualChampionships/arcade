package net.casual.arcade.interceptor

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.ReferenceCountUtil

@Sharable
public class InboundInterceptorHandler(
    private val interceptors: List<InboundInterceptor>
): ChannelInboundHandlerAdapter() {
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        var current = msg

        for (injector in this.interceptors) {
            when (val result = injector.intercept(ctx, current)) {
                InterceptorResult.Pass -> continue
                InterceptorResult.Cancel -> return
                is InterceptorResult.Replace -> {
                    if (current !== result.replacement) {
                        ReferenceCountUtil.release(current)
                    }
                    current = result.replacement
                }
            }
        }

        ctx.fireChannelRead(current)
    }
}