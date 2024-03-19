package net.casual.arcade.font.heads

import dev.fruxz.kojang.Kojang
import kotlinx.coroutines.future.future
import kotlinx.coroutines.runBlocking
import net.casual.arcade.Arcade
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.colour
import net.casual.arcade.utils.PlayerUtils
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO

public object PlayerHeadComponents {
    private val cache = ConcurrentHashMap<UUID, CompletableFuture<Component>>()
    private val nameToUUIDCache = ConcurrentHashMap<String, UUID>()
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
        if (this.cache.containsKey(uuid)) {
            return this.cache[uuid]!!
        }
        val skinUrl = player.server.sessionService.getTextures(player.gameProfile).skin?.url ?: return this.default
        val future = CompletableFuture.supplyAsync {
            this.generateHead(skinUrl)
        }
        this.cache[uuid] = future
        return future
    }

    public fun getHead(name: String): CompletableFuture<Component> {
        if (this.invalidNames.contains(name)) {
            return this.default
        }

        val player = PlayerUtils.player(name)
        if (player != null) {
            // This is faster since we don't have to look up uuid
            return this.getHead(player)
        }
        val cachedUUID = this.nameToUUIDCache[name]
        if (cachedUUID != null) {
            val cached = this.cache[cachedUUID]
            if (cached != null) {
                return cached
            }
        }

        // I would use co-routines for everything, but I want to
        // make this library Java friendly.
        // I think it's more appropriate to make everything futures
        val future = runBlocking {
            future {
                kotlin.runCatching { Kojang.getMojangUserProfile(name) }.getOrNull()
            }
        }.thenApply { profile ->
            if (profile == null) {
                this.invalidNames.add(name)
                PlayerHeadFont.STEVE_HEAD
            } else {
                val uuid = UUID.fromString(profile.uuid)
                this.nameToUUIDCache[name] = uuid
                val cached = this.cache[uuid]
                if (cached != null) {
                    cached.join()
                } else {
                    val component = this.generateHead(profile.textures.skin.url)
                    this.cache[uuid] = CompletableFuture.completedFuture(component)
                    component
                }
            }
        }
        return future
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
                    component.append(PlayerHeadFont.pixel(y).colour(image.getRGB(x + 8, y + 8)))
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
}