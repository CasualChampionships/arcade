/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.interceptor

import org.jetbrains.annotations.ApiStatus.Internal

public object ArcadeInterceptors {
    private val registered = ArrayList<InboundInterceptorHandler>()

    public fun register(interceptor: InboundInterceptor) {
        this.registered.add(InboundInterceptorHandler(interceptor))
    }

    @Internal
    @JvmStatic
    public fun getHandlers(): List<InboundInterceptorHandler> {
        return this.registered
    }
}