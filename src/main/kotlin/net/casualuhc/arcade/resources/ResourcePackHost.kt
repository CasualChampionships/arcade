package net.casualuhc.arcade.resources

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.net.InetAddress
import java.net.InetSocketAddress
import java.security.MessageDigest
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.random.Random

abstract class ResourcePackHost(
    threads: Int = 3
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val hosted = HashMap<String, HostedPack>()
    private val threadPool: ExecutorService

    private var server: HttpServer? = null

    init {
        val nameFactory = ThreadFactoryBuilder().setNameFormat("Pack-Host-%d").build()
        this.threadPool = Executors.newFixedThreadPool(threads, nameFactory)
    }

    fun getHostedPack(name: String): HostedPack? {
        val zipped = if (name.endsWith(".zip")) name else "$name.zip"
        return this.hosted[zipped]
    }

    fun start(hostIp: String? = null, hostPort: Int = 24464, randomise: Boolean = false) {
        this.threadPool.execute {
            val restart = this.server !== null
            this.hosted.clear()
            this.server?.stop(0)
            try {
                this.logger.info("${if (restart) "Restarting" else "Starting"} ResourcePackHost...")
                var ip = hostIp
                if (ip === null) {
                    this.logger.info("No IP address found for ResourcePackHost, using localhost instead")
                    ip = InetAddress.getLocalHost().hostAddress
                }

                val server = HttpServer.create(InetSocketAddress("0.0.0.0", hostPort), 0)
                server.executor = this.threadPool

                for (pack in this.getPacks()) {
                    val sub = if (randomise) Random.nextInt(Int.MAX_VALUE).toString() else pack.name

                    val url = "http://${ip}:${hostPort}/${sub}"
                    val hash = "%040x".format(
                        BigInteger(1, MessageDigest.getInstance("SHA-1").digest(pack.stream().readBytes()))
                    )
                    val hosted = HostedPack(pack, url, hash)
                    val zipped = if (pack.name.endsWith(".zip")) pack.name else "${pack.name}.zip"
                    this.hosted[zipped] = hosted

                    server.createContext("/$sub", Handler(hosted))

                    this.logger.info("Hosting pack: ${pack.name}: $url")
                }

                server.start()
                this.logger.info("ResourcePackHost successfully started")
            } catch (e: Exception) {
                this.logger.error("Failed to start the ResourcePackHost!", e)
            }
        }
    }

    fun shutdown() {
        this.server?.stop(0)
        this.threadPool.shutdownNow()
    }

    protected abstract fun getPacks(): Iterable<ReadablePack>

    class HostedPack(val pack: ReadablePack, val url: String, val hash: String)

    private inner class Handler(val hosted: HostedPack): HttpHandler {
        override fun handle(exchange: HttpExchange) {
            if ("GET" == exchange.requestMethod && this.hosted.pack.readable()) {
                val username = exchange.requestHeaders.getFirst("X-Minecraft-Username")
                if (username != null) {
                    logger.info("Player $username requested pack")
                } else {
                    logger.info("Non-player requested pack")
                }
                exchange.responseHeaders.add("User-Agent", "Java/ResourcePackHost")
                exchange.sendResponseHeaders(200, this.hosted.pack.length())
                exchange.responseBody.use { response ->
                    this.hosted.pack.stream().use { stream ->
                        stream.transferTo(response)
                    }
                }
            } else {
                exchange.sendResponseHeaders(400, 0)
            }
        }
    }
}