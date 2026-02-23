package net.casual.arcade.interceptor.http

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.stream.ChunkedWriteHandler
import net.casual.arcade.interceptor.InboundInterceptor
import net.casual.arcade.interceptor.InterceptorResult
import net.casual.arcade.interceptor.utils.sendError
import net.minecraft.network.HiddenByteBuf

public class InboundHttpMultiplexerInterceptor(
    private val handlers: List<InboundHttpHandler>
): InboundInterceptor() {
    override fun intercept(ctx: ChannelHandlerContext, msg: Any): InterceptorResult {
        val buf = HiddenByteBuf.unpack(msg) as? ByteBuf ?: return InterceptorResult.Pass

        if (this.isHttp(buf)) {
            this.setupHttpPipeline(ctx)
            ctx.fireChannelRead(buf)
            return InterceptorResult.Cancel
        }

        return InterceptorResult.Pass
    }

    private fun setupHttpPipeline(ctx: ChannelHandlerContext) {
        val pipeline = ctx.pipeline()
        val interceptorName = ctx.name()

        pipeline.addAfter(interceptorName, "http-codec", HttpServerCodec())
        pipeline.addAfter("http-codec", "http-aggregator", HttpObjectAggregator(UShort.MAX_VALUE.toInt()))
        pipeline.addAfter("http-aggregator", "http-chunked", ChunkedWriteHandler())

        pipeline.addAfter("http-chunked", "http-handler", object: SimpleChannelInboundHandler<FullHttpRequest>() {
            override fun channelRead0(context: ChannelHandlerContext, request: FullHttpRequest) {
                for (handler in handlers) {
                    if (handler.tryHandle(context, request)) {
                        return
                    }
                }

                context.sendError(request, HttpResponseStatus.NOT_FOUND)
            }
        })

        val handler = pipeline.get("http-handler")
        while (pipeline.last() != null && pipeline.last() !== handler) {
            pipeline.removeLast()
        }
    }

    private fun isHttp(buf: ByteBuf): Boolean {
        if (buf.readableBytes() < 3) {
            return false
        }

        val readerIndex = buf.readerIndex()
        val b0 = buf.getByte(readerIndex).toInt().toChar()
        val b1 = buf.getByte(readerIndex + 1).toInt().toChar()
        val b2 = buf.getByte(readerIndex + 2).toInt().toChar()

        return (b0 == 'G' && b1 == 'E' && b2 == 'T')
            || (b0 == 'P' && b1 == 'O' && b2 == 'S')
            || (b0 == 'P' && b1 == 'U' && b2 == 'T')
            || (b0 == 'H' && b1 == 'E' && b2 == 'A')
            || (b0 == 'O' && b1 == 'P' && b2 == 'T')
            || (b0 == 'P' && b1 == 'A' && b2 == 'T')
            || (b0 == 'D' && b1 == 'E' && b2 == 'L')
            || (b0 == 'T' && b1 == 'R' && b2 == 'A')
            || (b0 == 'C' && b1 == 'O' && b2 == 'N')
    }
}