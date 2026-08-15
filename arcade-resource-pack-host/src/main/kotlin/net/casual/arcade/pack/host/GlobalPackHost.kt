/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.host

import net.casual.arcade.interceptor.ArcadeInterceptors
import net.casual.arcade.interceptor.http.resource.HttpResourceInterceptor
import net.casual.arcade.utils.network.ResolvableURL
import java.net.URLEncoder
import java.util.concurrent.CompletableFuture

/**
 * This is a global pack hosting instance
 */
public object GlobalPackHost: PackHost() {
    public fun load() {
        ArcadeInterceptors.register(Interceptor)
    }

    override fun start(): CompletableFuture<Boolean> {
        return CompletableFuture.completedFuture(true)
    }

    override fun stop() {

    }

    override fun createUrl(name: String): ResolvableURL {
        val encoded = URLEncoder.encode(name, Charsets.UTF_8)
        return ResolvableURL.local("http", null, null, "arcade/packs/${encoded}")
    }

    private object Interceptor: HttpResourceInterceptor("/arcade/packs/", SERVER) {
        override fun getResource(path: String): HttpResource? {
            val pack = resolve(path)
            if (pack == null || !pack.readable()) {
                return null
            }
            return HttpResource(pack.stream(), "${pack.name}.zip", pack.length())
        }
    }
}