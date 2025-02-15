/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.host

import com.google.common.hash.Hashing
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.casual.arcade.host.core.HttpHost
import net.casual.arcade.host.pack.ReadablePack
import net.casual.arcade.host.pack.ReadablePackSupplier
import net.minecraft.Util
import java.io.InputStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty

public class PackHost(ip: String?, port: Int = DEFAULT_PORT, threads: Int = 1): HttpHost(ip, port, threads) {
    private val hostedByName = ConcurrentHashMap<String, HostedPack>()

    private val packs = Object2ObjectOpenHashMap<String, ReadablePack>()
    private val suppliers = ArrayList<ReadablePackSupplier>()

    public fun addPack(pack: ReadablePack): HostedPackRef {
        this.packs[pack.name] = pack
        val future = this.hostPack(pack)
        return HostedPackRef(pack.name, future)
    }

    public fun addSupplier(supplier: ReadablePackSupplier) {
        this.suppliers.add(supplier)
        for (pack in supplier.getPacks()) {
            this.hostPack(pack)
        }
    }

    public fun getHostedPack(name: String): HostedPack? {
        val zipped = if (name.endsWith(".zip")) name else "$name.zip"
        return this.hostedByName[zipped]
    }

    public fun removePack(name: String) {
        this.packs.remove(name)
        this.hostedByName.remove(name)
    }

    public fun removeSupplier(supplier: ReadablePackSupplier) {
        this.suppliers.remove(supplier)
    }

    public fun reload(): CompletableFuture<List<HostedPack>> {
        this.hostedByName.clear()

        val futures = ArrayList<CompletableFuture<HostedPack>>()
        for (pack in this.packs.values) {
            futures.add(this.hostPack(pack))
        }
        for (supplier in this.suppliers) {
            for (pack in supplier.getPacks()) {
                futures.add(this.hostPack(pack))
            }
        }
        return Util.sequenceFailFast(futures)
    }

    override fun getName(): String {
        return "resource-pack-host"
    }

    override fun onStart(server: HttpServer) {
        server.createContext("/") { exchange -> exchange.use(this::handleRequest) }
    }

    override fun onStop() {
        this.packs.clear()
        this.suppliers.clear()
        this.hostedByName.clear()
    }

    private fun hostPack(pack: ReadablePack): CompletableFuture<HostedPack> {
        return this.async {
            @Suppress("DEPRECATION")
            val hash = Hashing.sha1().hashBytes(pack.stream().use(InputStream::readBytes)).toString()

            val zipped = if (pack.name.endsWith(".zip")) pack.name else "${pack.name}.zip"
            val hosted = HostedPack(pack, "${this.getUrl()}/$zipped", hash)
            this.hostedByName[zipped] = hosted
            hosted
        }
    }

    private fun handleRequest(exchange: HttpExchange) {
        val name = exchange.requestURI.path.substring(1)
        if ("GET" != exchange.requestMethod) {
            exchange.sendResponseHeaders(400, -1)
            return
        }

        val hosted = this.getHostedPack(name)
        if (hosted == null || !hosted.pack.readable()) {
            exchange.sendResponseHeaders(400, -1)
            return
        }

        exchange.responseHeaders.add("User-Agent", "Kotlin/ResourcePackHost")
        exchange.sendResponseHeaders(200, hosted.pack.length())
        exchange.responseBody.use { response ->
            hosted.pack.stream().use { stream ->
                stream.transferTo(response)
            }
        }
    }

    public inner class HostedPackRef(
        private val name: String,
        public val future: CompletableFuture<HostedPack>
    ) {
        public val value: HostedPack
            get() = getHostedPack(this.name) ?: this.future.join()

        public operator fun getValue(any: Any?, property: KProperty<*>): HostedPack {
            return this.value
        }
    }

    public companion object {
        public const val DEFAULT_PORT: Int = 24464
    }
}