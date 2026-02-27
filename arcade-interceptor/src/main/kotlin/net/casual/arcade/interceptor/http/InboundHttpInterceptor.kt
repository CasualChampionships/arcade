/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.interceptor.http

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.stream.ChunkedWriteHandler
import net.casual.arcade.interceptor.InboundInterceptor
import net.minecraft.network.HiddenByteBuf

public abstract class InboundHttpInterceptor: InboundInterceptor() {
    final override fun intercept(ctx: ChannelHandlerContext, msg: Any): Boolean {
        val buf = HiddenByteBuf.unpack(msg) as? ByteBuf ?: return false

        val method = this.getHttpMethod(buf)
        if (method != null) {
            val uri = this.peekUri(buf, method)
            if (uri != null && this.canHandle(uri)) {
                this.setupHttpPipeline(ctx)
                ctx.pipeline().fireChannelRead(msg)
                return true
            }
        }

        return false
    }

    protected abstract fun canHandle(uri: String): Boolean

    protected abstract fun interceptHttp(ctx: ChannelHandlerContext, request: FullHttpRequest)

    private fun peekUri(buf: ByteBuf, method: String): String? {
        val readerIndex = buf.readerIndex()

        val start = readerIndex + method.length
        val toIndex = readerIndex + buf.readableBytes()

        if (start >= toIndex) {
            return null
        }

        val end = buf.indexOf(start, toIndex, ' '.code.toByte())
        if (end != -1) {
            val length = end - start
            if (length > 0) {
                return buf.toString(start, length, Charsets.UTF_8)
            }
        }
        return null
    }

    private fun setupHttpPipeline(ctx: ChannelHandlerContext) {
        val pipeline = ctx.pipeline()
        while (pipeline.last() != null) {
            pipeline.removeLast()
        }

        pipeline.addLast(HttpServerCodec())
        pipeline.addLast(HttpObjectAggregator(UShort.MAX_VALUE.toInt()))
        pipeline.addLast(ChunkedWriteHandler())

        pipeline.addLast(object: SimpleChannelInboundHandler<FullHttpRequest>() {
            override fun channelRead0(context: ChannelHandlerContext, request: FullHttpRequest) {
                interceptHttp(context, request)
            }
        })
    }

    private fun getHttpMethod(buf: ByteBuf): String? {
        if (buf.readableBytes() < 3) {
            return null
        }

        for (method in REQUEST_METHODS) {
            if (this.isRequestMethod(buf, method)) {
                return method
            }
        }
        return null
    }

    private fun isRequestMethod(buf: ByteBuf, method: String): Boolean {
        if (method.length > buf.readableBytes()) {
            return false
        }

        val readerIndex = buf.readerIndex()
        for (i in 0..< method.length) {
            val charAt = method[i]
            val byteAt = buf.getUnsignedByte(readerIndex + i).toInt()
            if (charAt.code != byteAt) {
                return false
            }
        }
        return true
    }

    private companion object {
        val REQUEST_METHODS = listOf("GET ", "POST ", "PUT ", "DELETE ", "PATCH ")
    }
}