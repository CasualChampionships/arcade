package net.casual.arcade.font.heads

import com.mojang.authlib.GameProfile
import net.casual.arcade.Arcade
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

    public fun getHeadOrDefault(player: ServerPlayer): Component {
        return this.getHead(player).getNow(PlayerHeadFont.STEVE_HEAD)
    }

    public fun getHeadOrDefault(name: String): Component {
        return this.getHead(name).getNow(PlayerHeadFont.STEVE_HEAD)
    }

    public fun getHead(player: ServerPlayer): CompletableFuture<Component> {
        val uuid = player.uuid
        if (this.uuidCache.containsKey(uuid)) {
            return this.uuidCache[uuid]!!
        }
        val skinUrl = this.getSkinUrl(player.gameProfile, player.server) ?: return this.default
        val future = CompletableFuture.supplyAsync {
            this.generateHead(skinUrl)
        }
        this.uuidCache[uuid] = future
        this.nameCache[player.scoreboardName] = future
        return future
    }

    public fun getHead(name: String, server: MinecraftServer = Arcade.getServer()): CompletableFuture<Component> {
        if (this.invalidNames.contains(name)) {
            return this.default
        }
        val cachedFuture = this.nameCache[name]
        if (cachedFuture != null) {
            return cachedFuture
        }

        val player = PlayerUtils.player(name)
        if (player != null) {
            // This is faster since we don't have to look up uuid
            return this.getHead(player)
        }

        val future = server.profileCache!!.getAsync(name).thenApply { optional ->
            if (optional.isEmpty) {
                invalidNames.add(name)
                return@thenApply PlayerHeadFont.STEVE_HEAD
            }
            val profile = optional.get()
            val uuid = profile.id
            val cached = uuidCache[uuid]
            if (cached != null) {
                return@thenApply cached.join()
            }
            val skinUrl = this.getSkinUrl(profile, server) ?: return@thenApply PlayerHeadFont.STEVE_HEAD
            val component = generateHead(skinUrl)
            uuidCache[uuid] = CompletableFuture.completedFuture(component)
            component
        }
        this.nameCache[name] = future
        return future
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