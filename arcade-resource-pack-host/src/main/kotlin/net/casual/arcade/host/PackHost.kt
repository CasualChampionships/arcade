/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.host

import com.google.common.hash.Hashing
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpsConfigurator
import com.sun.net.httpserver.HttpsServer
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.casual.arcade.host.pack.hosted.HostedPack
import net.casual.arcade.utils.network.ResolvableURL
import net.casual.arcade.host.pack.ReadablePack
import net.casual.arcade.host.pack.provider.PackProvider
import org.apache.logging.log4j.LogManager
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ssl.SSLContext
import kotlin.reflect.KProperty

/**
 * Class that represents a pack hosting server.
 *
 * @see PackHost.create
 */
public abstract class PackHost {
    private val packs = Object2ObjectOpenHashMap<String, ReadablePack>()
    private val hosted = ConcurrentHashMap<String, HostedPack>()

    private val providers = LinkedHashSet<PackProvider>()

    protected val executor: ExecutorService = Executors.newSingleThreadExecutor(
        ThreadFactoryBuilder().setNameFormat("resource-pack-host-%d").setDaemon(true).build()
    )

    public fun add(pack: ReadablePack): HostedPackRef {
        this.packs[pack.name] = pack
        val future = this.hostPack(pack)
        return HostedPackRef(future) { this.get(pack.name) }
    }

    public fun add(provider: PackProvider) {
        this.providers.add(provider)
    }

    public fun remove(name: String) {
        this.packs.remove(name)
        this.hosted.remove(name)
    }

    public fun remove(provider: PackProvider) {
        this.providers.remove(provider)
    }

    public fun get(name: String): HostedPack? {
        return this.hosted[name]
    }

    public fun resolve(name: String): ReadablePack? {
        val readable = this.packs[name]
        if (readable != null) {
            return readable
        }
        for (provider in this.providers) {
            return provider.get(name) ?: continue
        }
        return null
    }

    public abstract fun start(): CompletableFuture<Boolean>

    public abstract fun stop()

    public abstract fun createUrl(name: String): ResolvableURL

    protected fun <T> async(block: () -> T): CompletableFuture<T> {
        return CompletableFuture.supplyAsync(block, this.executor).exceptionally { exception ->
            this.handleException(exception)
            throw exception
        }
    }

    protected fun handleException(throwable: Throwable) {
        logger.error("Exception occurred during pack hosting", throwable)
    }

    private fun hostPack(pack: ReadablePack): CompletableFuture<HostedPack> {
        return this.async {
            @Suppress("DEPRECATION")
            val hash = Hashing.sha1().hashBytes(pack.stream().use(InputStream::readBytes)).toString()

            val hosted = HostedPack(pack, this.createUrl(pack.name), hash)
            this.hosted[pack.name] = hosted
            hosted
        }
    }

    public class HostedPackRef internal constructor(
        public val future: CompletableFuture<HostedPack>,
        private val getter: () -> HostedPack?,
    ) {
        public val value: HostedPack
            get() = this.getter.invoke() ?: this.future.join()

        public operator fun getValue(any: Any?, property: KProperty<*>): HostedPack {
            return this.value
        }
    }

    private class Impl(
        val port: Int,
        val server: HttpServer
    ): PackHost() {
        private lateinit var starting: CompletableFuture<Boolean>

        val isSecure: Boolean
            get() = this.server is HttpsServer

        override fun start(): CompletableFuture<Boolean> {
            if (!this::starting.isInitialized) {
                this.starting = this.async {
                    try {
                        this.server.executor = this.executor
                        this.server.createContext("/") { exchange ->
                            exchange.use(this::handleRequest)
                        }
                        this.server.start()
                        true
                    } catch (exception: Exception) {
                        this.handleException(exception)
                        false
                    }
                }
            }
            return this.starting
        }

        override fun stop() {
            this.executor.shutdownNow()
            this.server.stop(0)
        }

        override fun createUrl(name: String): ResolvableURL {
            val protocol = if (this.isSecure) "https" else "http"
            val encoded = URLEncoder.encode(name, Charsets.UTF_8)
            return ResolvableURL.local(protocol, null, this.port, encoded)
        }

        private fun handleRequest(exchange: HttpExchange) {
            val name = exchange.requestURI.path.substring(1)
            if ("GET" != exchange.requestMethod) {
                exchange.sendResponseHeaders(400, -1)
                return
            }

            val pack = this.resolve(URLDecoder.decode(name, Charsets.UTF_8))
            if (pack == null || !pack.readable()) {
                exchange.sendResponseHeaders(400, -1)
                return
            }

            exchange.responseHeaders.add("user-agent", USER_AGENT)
            exchange.sendResponseHeaders(200, pack.length())
            exchange.responseBody.use { response ->
                pack.stream().use { stream ->
                    stream.transferTo(response)
                }
            }
        }
    }

    public companion object {
        private const val DEFAULT_PORT: Int = 24464

        internal const val USER_AGENT = "kotlin/arcade-pack-download-host"

        internal val logger = LogManager.getLogger("ResourcePackHost")

        /**
         * Creates an instance of [PackHost] with a specified port.
         * Supports using SSL by passing in a [context].
         *
         * @param port The port of the pack host.
         * @param context The [SSLContext], null by default.
         * @return A [PackHost] instance.
         */
        @JvmStatic
        @JvmOverloads
        public fun create(
            port: Int = DEFAULT_PORT,
            context: SSLContext? = null
        ): PackHost {
            val address = InetSocketAddress("0.0.0.0", port)
            val server: HttpServer
            if (context == null) {
                server = HttpServer.create(address, 0)
            } else {
                server = HttpsServer.create(address, 0)
                server.httpsConfigurator = HttpsConfigurator(context)
            }
            return Impl(port, server)
        }
    }
}