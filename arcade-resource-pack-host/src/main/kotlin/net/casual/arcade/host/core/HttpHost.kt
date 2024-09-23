package net.casual.arcade.host.core

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.sun.net.httpserver.HttpServer
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

public abstract class HttpHost(
    ip: String?,
    public val port: Int,
    private val threads: Int
) {
    private val builder = ThreadFactoryBuilder().setNameFormat("${this.getName()}-%d").build()

    public val ip: String = ip ?: getLocalIp()

    private val server by lazy {
        HttpServer.create(InetSocketAddress("0.0.0.0", this.port), 0)
    }

    private val pool by lazy {
        Executors.newFixedThreadPool(this.threads, this.builder)
    }

    private var started = false

    public open fun getName(): String {
        return this::class.java.simpleName
    }

    public fun getUrl(): String {
        @Suppress("HttpUrlsUsage")
        return "http://${this.ip}:${this.port}"
    }

    public fun start(): CompletableFuture<Boolean> {
        if (this.started) {
            throw IllegalStateException("${this.getName()} (:${this.port}) has already started")
        }
        return this.async {
            try {
                this.server.executor = this.pool
                this.onStart(this.server)
                this.server.start()
                true
            } catch (exception: Exception) {
                this.onException(exception)
                false
            }
        }
    }

    public fun stop() {
        this.pool.shutdownNow()
        this.server.stop(0)

        this.onStop()
    }

    protected fun <T> async(block: () -> T): CompletableFuture<T> {
        return CompletableFuture.supplyAsync(block, this.pool).exceptionally { exception ->
            this.onException(exception)
            throw exception
        }
    }

    protected open fun onStart(server: HttpServer) {

    }

    protected open fun onStop() {

    }

    protected open fun onException(throwable: Throwable) {

    }

    private companion object {
        fun getLocalIp(): String {
            return InetAddress.getLocalHost().hostAddress
        }
    }
}