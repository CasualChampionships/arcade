package net.casual.arcade.interceptor.http.file

import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.DefaultFileRegion
import io.netty.handler.codec.http.*
import net.casual.arcade.interceptor.http.InboundHttpHandler
import net.casual.arcade.interceptor.utils.sendError
import java.io.RandomAccessFile
import java.net.URLDecoder
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.isDirectory

public open class HttpFileServerHandler(
    private val root: Path,
    private val prefix: String = "/files/",
    private val server: String = "kotlin/arcade-file-server"
): InboundHttpHandler {
    override fun isRelevant() {

    }

    protected open fun isValidTarget(path: Path): Boolean {
        return true
    }

    override fun tryHandle(ctx: ChannelHandlerContext, request: FullHttpRequest): Boolean {
        if (request.method() != HttpMethod.GET) {
            return false
        }

        val decoded = try {
            URLDecoder.decode(request.uri().substringBefore("?"), Charsets.UTF_8)
        } catch (_: Exception) {
            return false
        }

        if (!decoded.startsWith(this.prefix)) {
            return false
        }

        val target = this.root.resolve(decoded.removePrefix(this.prefix)).normalize()
        if (!target.startsWith(this.root.normalize())) {
            ctx.sendError(request, HttpResponseStatus.FORBIDDEN)
            return true
        }

        if (!target.exists() || target.isDirectory() || !this.isValidTarget(target)) {
            ctx.sendError(request, HttpResponseStatus.NOT_FOUND)
            return true
        }

        val file = try {
            RandomAccessFile(target.toFile(), "r")
        } catch (_: Exception) {
            ctx.sendError(request, HttpResponseStatus.INTERNAL_SERVER_ERROR)
            return true
        }

        val response = DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
        HttpUtil.setContentLength(response, target.fileSize())
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream")
        response.headers().set(HttpHeaderNames.SERVER, this.server)

        val keepAlive = HttpUtil.isKeepAlive(request)
        if (!keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE)
        } else if (request.protocolVersion() == HttpVersion.HTTP_1_0) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
        }

        ctx.write(response)

        ctx.write(DefaultFileRegion(file.channel, 0, target.fileSize()))
        val lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
        lastContentFuture.addListener { file.close() }

        if (!keepAlive) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE)
        }
        return true
    }
}