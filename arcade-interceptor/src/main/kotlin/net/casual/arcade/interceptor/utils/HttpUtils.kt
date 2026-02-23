/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.interceptor.utils

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpUtil
import io.netty.handler.codec.http.HttpVersion

public fun ChannelHandlerContext.sendHttpError(status: HttpResponseStatus, keepAlive: Boolean = false) {
    val content = Unpooled.copiedBuffer("Failure: $status\r\n", Charsets.UTF_8)
    val response = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content)
    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8")
    HttpUtil.setContentLength(response, content.readableBytes().toLong())

    if (keepAlive) {
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
    } else {
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE)
    }

    val future = this.writeAndFlush(response)
    if (!keepAlive) {
        future.addListener(ChannelFutureListener.CLOSE)
    }
}