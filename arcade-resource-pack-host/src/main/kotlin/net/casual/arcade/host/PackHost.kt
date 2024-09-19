package net.casual.arcade.host

import com.google.common.hash.Hashing
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import net.casual.arcade.host.core.HttpHost
import net.casual.arcade.host.pack.ReadablePack
import net.casual.arcade.host.pack.ReadablePackSupplier
import java.io.InputStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future
import kotlin.collections.ArrayList
import kotlin.reflect.KProperty

public class PackHost(ip: String?, port: Int, threads: Int): HttpHost(ip, port, threads) {
    private val hostedByName = ConcurrentHashMap<String, HostedPack>()
    private val hostedByHash = ConcurrentHashMap<String, HostedPack>()

    private val packs = ArrayList<ReadablePack>()
    private val suppliers = ArrayList<ReadablePackSupplier>()

    public fun addPack(pack: ReadablePack): HostedPackRef {
        this.packs.add(pack)
        val future = this.hostPack(pack)
        return HostedPackRef(pack.name, future)
    }

    public fun addSupplier(supplier: ReadablePackSupplier) {
        this.suppliers.add(supplier)
    }

    public fun getHostedPack(name: String): HostedPack? {
        val zipped = if (name.endsWith(".zip")) name else "$name.zip"
        return this.hostedByName[zipped]
    }

    public fun reload() {
        this.hostedByName.clear()
        this.hostedByHash.clear()

        for (pack in this.packs) {
            this.hostPack(pack)
        }
        for (supplier in this.suppliers) {
            for (pack in supplier.getPacks()) {
                this.hostPack(pack)
            }
        }
    }

    override fun getName(): String {
        return "ResourcePackHost"
    }

    override fun onStart(server: HttpServer) {
        server.createContext("/") { exchange -> exchange.use(this::handleRequest) }
    }

    override fun onStop() {
        this.packs.clear()
        this.suppliers.clear()
        this.hostedByName.clear()
        this.hostedByHash.clear()
    }

    private fun hostPack(pack: ReadablePack): CompletableFuture<HostedPack> {
        return this.async {
            @Suppress("DEPRECATION")
            val hash = Hashing.sha1().hashBytes(pack.stream().use(InputStream::readBytes)).toString()
            val hosted = HostedPack(pack, "${this.getUrl()}/$hash", hash)
            this.hostedByHash[hash] = hosted

            val zipped = if (pack.name.endsWith(".zip")) pack.name else "${pack.name}.zip"
            this.hostedByName[zipped] = hosted
            hosted
        }
    }

    private fun handleRequest(exchange: HttpExchange) {
        val hash = exchange.requestURI.path.substring(1)
        if ("GET" != exchange.requestMethod || hash.length != 40) {
            exchange.sendResponseHeaders(400, -1)
            return
        }

        val hosted = this.hostedByHash[hash]
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
        private val future: CompletableFuture<HostedPack>
    ) {
        public operator fun getValue(any: Any?, property: KProperty<*>): HostedPack {
            return getHostedPack(this.name) ?: this.future.join()
        }
    }
}