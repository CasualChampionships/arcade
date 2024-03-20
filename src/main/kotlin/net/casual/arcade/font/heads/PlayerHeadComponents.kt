package net.casual.arcade.font.heads

import com.mojang.authlib.GameProfile
import net.casual.arcade.Arcade
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerJoinEvent
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.colour
import net.casual.arcade.utils.PlayerUtils
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.awt.Color
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO

public object PlayerHeadComponents {
    private val uuidCache = ConcurrentHashMap<UUID, CompletableFuture<Component>>()
    private val nameCache = ConcurrentHashMap<String, CompletableFuture<Component>>()
    private val invalidNames = ConcurrentHashMap.newKeySet<String>()

    private val default = CompletableFuture.completedFuture<Component>(PlayerHeadFont.STEVE_HEAD)

    init {
        GlobalEventHandler.register<PlayerJoinEvent> {
            this.invalidateHead(it.player)
        }
    }

    public fun getHeadOrDefault(player: ServerPlayer): Component {
        return this.getHead(player).getNow(PlayerHeadFont.STEVE_HEAD)
    }

    public fun getHeadOrDefault(name: String): Component {
        return this.getHead(name).getNow(PlayerHeadFont.STEVE_HEAD)
    }

    public fun getHead(
        player: ServerPlayer,
        force: Boolean = false
    ): CompletableFuture<Component> {
        val uuid = player.uuid
        val name = player.scoreboardName
        val exists = this.uuidCache.containsKey(uuid)
        if (!force && exists) {
            return this.uuidCache[uuid]!!
        }
        val skinUrl = this.getSkinUrl(player.gameProfile, player.server) ?: return this.default
        val future = CompletableFuture.supplyAsync {
            val component = this.generateHead(skinUrl)
            if (exists) {
                this.cacheHead(name, uuid, CompletableFuture.completedFuture(component))
            }
            component
        }
        if (!exists) {
            this.cacheHead(name, uuid, future)
        }
        return future
    }

    public fun getHead(
        name: String,
        server: MinecraftServer = Arcade.getServer(),
        force: Boolean = false
    ): CompletableFuture<Component> {
        if (this.invalidNames.contains(name)) {
            return this.default
        }
        val exists = this.nameCache.containsKey(name)
        if (!force && exists) {
            return this.nameCache[name]!!
        }

        val player = PlayerUtils.player(name)
        if (player != null) {
            // This is faster since we don't have to look up uuid
            return this.getHead(player, force)
        }

        val future = server.profileCache!!.getAsync(name).thenApply { optional ->
            if (optional.isEmpty) {
                invalidNames.add(name)
                return@thenApply PlayerHeadFont.STEVE_HEAD
            }
            val uuid = optional.get().id
            // If they're in the uuid cache, they should've been
            // in the nameCache too, but we might as well check...
            val cached = uuidCache[uuid]
            if (!force && cached != null) {
                return@thenApply cached.join()
            }

            // The previous profile didn't have textures
            val profile = server.sessionService.fetchProfile(uuid, true)?.profile
                ?: return@thenApply PlayerHeadFont.STEVE_HEAD
            val skinUrl = this.getSkinUrl(profile, server) ?: return@thenApply PlayerHeadFont.STEVE_HEAD
            val component = generateHead(skinUrl)
            if (exists) {
                this.cacheHead(name, uuid, CompletableFuture.completedFuture(component))
            }
            component
        }
        if (!exists) {
            this.nameCache[name] = future
        }
        return future
    }


    internal fun invalidateHead(player: ServerPlayer) {
        if (this.uuidCache.containsKey(player.uuid)) {
            // This will overwrite any head that was previously generated...
            this.getHead(player, true)
        }
    }

    private fun cacheHead(name: String, uuid: UUID, future: CompletableFuture<Component>) {
        this.nameCache[name] = future
        this.uuidCache[uuid] = future
    }

    private fun getSkinUrl(profile: GameProfile, server: MinecraftServer): String? {
        return server.sessionService.getTextures(profile).skin?.url
    }

    private fun generateHead(skinTextureUrl: String): Component {
        try {
            val image = ImageIO.read(URL(skinTextureUrl))
            val component = Component.empty()
            for (y in 0..7) {
                for (x in 0..7) {
                    if (x != 0) {
                        component.append(ComponentUtils.space(-1))
                    }
                    val hat = Color(image.getRGB(x + 40, y + 8), true)
                    val base = Color(image.getRGB(x + 8, y + 8), true)
                    component.append(PlayerHeadFont.pixel(y).colour(base.overlayWith(hat).rgb))
                }
                if (y != 7) {
                    component.append(ComponentUtils.space(-9))
                }
            }
            return component
        } catch (e: IOException) {
            Arcade.logger.error("Failed to generate head texture from url: $skinTextureUrl", e)
            return PlayerHeadFont.STEVE_HEAD
        }
    }

    private fun Color.overlayWith(overlay: Color): Color {
        val alphaRatio = overlay.alpha.toFloat() / 255
        val invAlphaRatio = 1 - alphaRatio

        val r = (overlay.red * alphaRatio + this.red * invAlphaRatio).toInt()
        val g = (overlay.green * alphaRatio + this.green * invAlphaRatio).toInt()
        val b = (overlay.blue * alphaRatio + this.blue * invAlphaRatio).toInt()
        val a = maxOf(overlay.alpha, this.alpha)

        return Color(r, g, b, a)
    }
}