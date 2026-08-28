/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.host

import com.google.common.hash.Hashing
import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpsConfigurator
import com.sun.net.httpserver.HttpsServer
import net.casual.arcade.pack.host.provider.PackProvider
import net.casual.arcade.utils.network.ResolvableURL
import org.apache.logging.log4j.LogManager
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ssl.SSLContext

/**
 * Class that represents a pack hosting server.
 *
 * @see PackHost.create
 */
public abstract class PackHost {
    private val hosted = ConcurrentHashMap<UUID, HostedPackRef>()

    private val providers = LinkedHashSet<PackProvider>()

    protected val executor: ExecutorService = Executors.newSingleThreadExecutor(
        ThreadFactoryBuilder().setNameFormat("resource-pack-host-%d").setDaemon(true).build()
    )

    /**
     * Hosts a given [pack] under a randomly generated id.
     *
     * @param pack The pack to host.
     * @return A reference to the pack being hosted.
     */
    public fun add(pack: ReadablePack): HostedPackRef {
        return this.add(UUID.randomUUID()) { pack }
    }

    /**
     * Hosts the pack provided by [pack] under a given [uuid].
     *
     * [pack] is lazily invoked only if no pack under the same
     * [uuid] is already being hosted.
     *
     * @param uuid The unique id to host the pack under.
     * @param pack The pack to host.
     * @return A reference to the pack being hosted.
     */
    public fun add(uuid: UUID, pack: () -> ReadablePack): HostedPackRef {
        val ref = HostedPackRef(uuid)
        val previous = this.hosted.putIfAbsent(uuid, ref)
        if (previous != null) {
            return previous
        }
        val future = this.async { this.hostPack(uuid, pack.invoke()) }
        future.whenComplete { hosted, exception ->
            if (exception != null) {
                this.hosted.remove(uuid, ref)
                ref.completeExceptionally(exception)
            } else {
                ref.complete(hosted)
            }
        }
        return ref
    }

    /**
     * Adds a [provider] which can lazily provide packs by name.
     *
     * @param provider The provider to add.
     */
    public fun add(provider: PackProvider) {
        this.providers.add(provider)
    }

    /**
     * Stops hosting the pack with a given [uuid].
     *
     * @param uuid The unique id of the pack.
     * @return Whether a pack was removed.
     */
    public fun remove(uuid: UUID): Boolean {
        return this.hosted.remove(uuid) != null
    }

    /**
     * Stops hosting the pack referenced by [ref].
     *
     * @param ref The reference to the hosted pack.
     * @return Whether the pack was removed.
     */
    public fun remove(ref: HostedPackRef): Boolean {
        return this.hosted.remove(ref.uuid, ref)
    }

    /**
     * Removes a given [provider].
     *
     * @param provider The provider to remove.
     */
    public fun remove(provider: PackProvider) {
        this.providers.remove(provider)
    }

    /**
     * Gets the reference to the pack hosted under a given [uuid].
     *
     * @param uuid The unique id of the pack.
     * @return The reference to the hosted pack, or `null` if none is hosted.
     */
    public fun get(uuid: UUID): HostedPackRef? {
        return this.hosted[uuid]
    }

    /**
     * Resolves the pack that is being served at a given [path].
     *
     * @param path The path of the pack, either the id of a hosted
     *   pack or the name of a pack given by a [PackProvider].
     * @return The readable pack, or `null` if nothing is served there.
     */
    public fun resolve(path: String): ReadablePack? {
        val uuid = this.tryParseUUID(path)
        if (uuid != null) {
            val hosted = this.hosted[uuid]?.getNow()
            if (hosted != null) {
                return hosted.pack
            }
        }
        for (provider in this.providers) {
            return provider.get(path) ?: continue
        }
        return null
    }

    public abstract fun start(): CompletableFuture<Boolean>

    public abstract fun stop()

    public abstract fun createUrl(path: String): ResolvableURL

    protected fun <T> async(block: () -> T): CompletableFuture<T> {
        return CompletableFuture.supplyAsync(block, this.executor).exceptionally { exception ->
            this.handleException(exception)
            throw exception
        }
    }

    protected fun handleException(throwable: Throwable) {
        logger.error("Exception occurred during pack hosting", throwable)
    }

    private fun hostPack(uuid: UUID, pack: ReadablePack): HostedPack {
        @Suppress("DEPRECATION")
        val hash = pack.hash() ?: Hashing.sha1().hashBytes(pack.stream().use(InputStream::readBytes)).toString()
        return HostedPack(uuid, pack, this.createUrl(uuid.toString()), hash)
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

        override fun createUrl(path: String): ResolvableURL {
            val protocol = if (this.isSecure) "https" else "http"
            val encoded = URLEncoder.encode(path, Charsets.UTF_8)
            return ResolvableURL.local(protocol, null, this.port, encoded)
        }

        private fun handleRequest(exchange: HttpExchange) {
            val path = exchange.requestURI.path.substring(1)
            if ("GET" != exchange.requestMethod) {
                exchange.sendResponseHeaders(400, -1)
                return
            }

            val pack = this.resolve(URLDecoder.decode(path, Charsets.UTF_8))
            if (pack == null || !pack.readable()) {
                exchange.sendResponseHeaders(400, -1)
                return
            }

            exchange.responseHeaders.add("server", SERVER)
            exchange.sendResponseHeaders(200, pack.length())
            exchange.responseBody.use { response ->
                pack.stream().use { stream ->
                    stream.transferTo(response)
                }
            }
        }
    }

    private fun tryParseUUID(string: String): UUID? {
        return try {
            UUID.fromString(string)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    public companion object {
        private const val DEFAULT_PORT: Int = 24464

        internal const val SERVER = "kotlin/arcade-pack-download-host"

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