/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.interceptor.http.resource

import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import io.netty.handler.stream.ChunkedStream
import net.casual.arcade.interceptor.http.InboundHttpInterceptor
import net.casual.arcade.interceptor.utils.sendHttpError
import java.io.InputStream
import java.net.URLDecoder

public abstract class HttpResourceInterceptor(
    private val prefix: String,
    private val server: String = "kotlin/arcade-resource-server"
): InboundHttpInterceptor() {
    override fun canHandle(uri: String): Boolean {
        val decoded = try {
            URLDecoder.decode(uri.substringBefore("?"), Charsets.UTF_8)
        } catch (_: Exception) {
            return false
        }
        return decoded.startsWith(this.prefix)
    }

    override fun interceptHttp(ctx: ChannelHandlerContext, request: FullHttpRequest) {
        if (request.method() != HttpMethod.GET) {
            ctx.sendHttpError(HttpResponseStatus.METHOD_NOT_ALLOWED)
            return
        }

        val decoded = try {
            URLDecoder.decode(request.uri().substringBefore("?"), Charsets.UTF_8)
        } catch (_: Exception) {
            ctx.sendHttpError(HttpResponseStatus.BAD_REQUEST)
            return
        }

        val path = decoded.removePrefix(this.prefix)
        val resource = try {
            this.getResource(path)
        } catch (_: Exception) {
            ctx.sendHttpError(HttpResponseStatus.INTERNAL_SERVER_ERROR)
            return
        }

        if (resource == null) {
            ctx.sendHttpError(HttpResponseStatus.NOT_FOUND)
            return
        }

        val keepAlive = HttpUtil.isKeepAlive(request)
        val response = DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream")
        response.headers().set(HttpHeaderNames.SERVER, this.server)
        if (resource.name != null) {
            response.headers().set(HttpHeaderNames.CONTENT_DISPOSITION, "attachment; filename=\"${resource.name}\"")
        }

        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
        } else {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE)
        }

        if (resource.size != 0L) {
            HttpUtil.setContentLength(response, resource.size)
        } else {
            HttpUtil.setTransferEncodingChunked(response, true)
        }

        ctx.write(response)

        val streamFuture = ctx.writeAndFlush(ChunkedStream(resource.stream))
        streamFuture.addListener { resource.stream.close() }

        if (!keepAlive) {
            streamFuture.addListener(ChannelFutureListener.CLOSE)
        }
    }

    protected abstract fun getResource(path: String): HttpResource?

    protected data class HttpResource(val stream: InputStream, val name: String? = null, val size: Long = 0)
}