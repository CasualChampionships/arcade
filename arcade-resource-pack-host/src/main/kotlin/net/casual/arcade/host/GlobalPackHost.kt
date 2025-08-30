/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.host

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.stream.ChunkedStream
import net.casual.arcade.utils.network.ResolvableURL
import net.mcbrawls.inject.api.InjectorContext
import net.mcbrawls.inject.fabric.InjectFabric
import net.mcbrawls.inject.http.HttpByteBuf
import net.mcbrawls.inject.http.HttpInjector
import net.mcbrawls.inject.http.HttpRequest
import java.io.InputStream
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

/**
 * This is a global pack hosting instance
 */
public object GlobalPackHost: PackHost() {
    public fun load() {
        InjectFabric.INSTANCE.registerInjector(Injector)
    }

    override fun start(): CompletableFuture<Boolean> {
        throw IllegalStateException("Cannot manually start GlobalPackHost")
    }

    override fun stop() {
        throw IllegalStateException("Cannot manually stop GlobalPackHost")
    }

    override fun createUrl(name: String): ResolvableURL {
        val encoded = URLEncoder.encode(name, Charsets.UTF_8)
        return ResolvableURL.local("http", null, null, "arcade/packs/${encoded}")
    }

    private fun download(ctx: ChannelHandlerContext, stream: InputStream) {
        val buf = HttpByteBuf.httpBuf(ctx)
        buf.writeStatusLine("1.1", 200, "OK")
        buf.writeHeader("user-agent", USER_AGENT)
        buf.writeHeader("content-type", "application/octet-stream")
        buf.writeHeader("transfer-encoding", "chunked")
        buf.writeText("")
        ctx.writeAndFlush(buf.inner())

        val chunked = ChunkedStream(stream)
        ctx.writeAndFlush(chunked)

        var read: Int
        val buffer = ByteArray(8192)
        while (true) {
            read = stream.read(buffer)
            if (read == -1) {
                break
            }

            val chunkHeader = "${read.toString(16)}\r\n"
            ctx.write(Unpooled.copiedBuffer(chunkHeader, StandardCharsets.US_ASCII))
            ctx.write(Unpooled.copiedBuffer(buffer, 0, read))
            ctx.write(Unpooled.copiedBuffer("\r\n", StandardCharsets.US_ASCII))
        }

        ctx.writeAndFlush(Unpooled.copiedBuffer("0\r\n\r\n", StandardCharsets.US_ASCII))
    }

    private object Injector: HttpInjector() {
        private val regex = Regex("""^/arcade/packs/(.*)$""")

        override fun isRelevant(ctx: InjectorContext, request: HttpRequest): Boolean {
            return request.requestURI.matches(this.regex)
        }

        override fun onRead(ctx: ChannelHandlerContext, buf: ByteBuf): Boolean {
            val request = HttpRequest.parse(buf)
            if ("GET" != request.requestMethod) {
                return super.onRead(ctx, buf)
            }

            val match = this.regex.find(request.requestURI)!!
            val name = match.groups[1]!!.value
            val pack = resolve(URLDecoder.decode(name, Charsets.UTF_8))
            if (pack == null || !pack.readable()) {
                return super.onRead(ctx, buf)
            }

            pack.stream().use {
                download(ctx, it)
            }
            return true
        }

        override fun intercept(ctx: ChannelHandlerContext, request: HttpRequest): HttpByteBuf {
            val buf = HttpByteBuf.httpBuf(ctx)
            buf.writeStatusLine("1.1", 404, "Not Found")
            return buf
        }
    }
}