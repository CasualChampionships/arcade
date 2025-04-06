/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.host

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.stream.ChunkedStream
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.casual.arcade.host.data.ResolvablePackURL
import net.casual.arcade.utils.ArcadeUtils
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
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * This is a global pack hosting instance
 */
public object GlobalPackHost: PackHost() {
    private val config = this.readConfig()

    public fun load() {
        InjectFabric.INSTANCE.registerInjector(Injector)
    }

    public fun getConfiguredIp(): String? {
        return this.config.ip
    }

    override fun start(): CompletableFuture<Boolean> {
        throw IllegalStateException("Cannot manually start GlobalPackHost")
    }

    override fun stop() {
        throw IllegalStateException("Cannot manually stop GlobalPackHost")
    }

    override fun createUrl(name: String): ResolvablePackURL {
        val encoded = URLEncoder.encode(name, Charsets.UTF_8)
        return ResolvablePackURL.local("http", null, null, "arcade/packs/${encoded}")
    }

    private fun download(ctx: ChannelHandlerContext, stream: InputStream) {
        val buf = HttpByteBuf.httpBuf(ctx)
        buf.writeStatusLine("1.1", 200, "OK")
        buf.writeHeader("user-agent", "kotlin/arcade-pack-download-host")
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

    private fun readConfig(): Config {
        val path = ArcadeUtils.path.resolve("resource-pack-host.json")
        if (path.isRegularFile()) {
            try {
                return Json.decodeFromString<Config>(path.readText())
            } catch (_: Exception) {

            }
        }
        val config = Config()
        try {
            path.writeText(Json.encodeToString(config))
        } catch (exception: Exception) {
            logger.error("Failed to write pack host config", exception)
        }
        return config
    }

    @Serializable
    @OptIn(ExperimentalSerializationApi::class)
    private class Config(
        @EncodeDefault(EncodeDefault.Mode.ALWAYS)
        val ip: String? = null
    )

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
            val hosted = getHostedPack(URLDecoder.decode(name, Charsets.UTF_8))
            if (hosted == null || !hosted.pack.readable()) {
                return super.onRead(ctx, buf)
            }

            hosted.pack.stream().use {
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