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
import kotlin.reflect.KProperty

public class PackHost(ip: String?, port: Int = DEFAULT_PORT, threads: Int = 1): HttpHost(ip, port, threads) {
    private val hostedByName = ConcurrentHashMap<String, HostedPack>()
    private val hostedByHash = ConcurrentHashMap<String, HostedPack>()

    private val packs = HashMap<String, ReadablePack>()
    private val suppliers = ArrayList<ReadablePackSupplier>()

    public fun addPack(pack: ReadablePack): HostedPackRef {
        this.packs[pack.name] = pack
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

    public fun reload(): CompletableFuture<Void> {
        this.hostedByName.clear()
        this.hostedByHash.clear()

        val futures = ArrayList<CompletableFuture<*>>()
        for (pack in this.packs.values) {
            futures.add(this.hostPack(pack))
        }
        for (supplier in this.suppliers) {
            for (pack in supplier.getPacks()) {
                futures.add(this.hostPack(pack))
            }
        }
        return CompletableFuture.allOf(*futures.toTypedArray())
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