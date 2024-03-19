package net.casual.arcade.font.heads

import dev.fruxz.kojang.Kojang
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
import javax.imageio.ImageIO

public object PlayerHeadComponents {
    private val futures = HashMap<UUID, CompletableFuture<Component>>()
    private val uuidCache = HashMap<UUID, Component?>()
    private val namedCache = HashMap<String, Component?>()

    public fun getHeadFor(player: ServerPlayer): Component {
        val uuid = player.uuid
        if (this.uuidCache.containsKey(uuid)) {
            return this.uuidCache[uuid] ?: PlayerHeadFont.STEVE_HEAD
        }
        return this.generateHead(player.scoreboardName, player.uuid, true) {
            player.server.sessionService.getTextures(player.gameProfile).skin?.url
        }
    }

    public fun getHeadFor(name: String): Component {
        if (this.namedCache.containsKey(name)) {
            return this.namedCache[name] ?: PlayerHeadFont.STEVE_HEAD
        }
        val player = PlayerUtils.player(name)
        if (player != null) {
            return this.getHeadFor(player)
        }
        return runBlocking {
            val profile = Kojang.getMojangUserProfile(name)
            if (profile == null) {
                PlayerHeadFont.STEVE_HEAD
            } else {
                generateHead(profile.username, UUID.fromString(profile.uuid), true) {
                    profile.textures.skin.url
                }
            }
        }
    }

    private fun generateHead(
        name: String,
        uuid: UUID,
        block: Boolean,
        supplier: () -> String?
    ): Component {
        val future = this.futures[uuid]
        if (future == null) {
            val url = supplier()
            if (url == null) {
                this.uuidCache[uuid] = null
                this.namedCache[name] = null
            } else {
                val newFuture = this.generateHeadAsync(url)
                this.futures[uuid] = newFuture
                if (block) {
                    return newFuture.join()
                }
            }
            return PlayerHeadFont.STEVE_HEAD
        }

        if (!future.isDone) {
            if (block) {
                return future.join()
            }
            return PlayerHeadFont.STEVE_HEAD
        }
        this.futures.remove(uuid)
        if (future.isCompletedExceptionally || future.isCancelled) {
            this.uuidCache[uuid] = null
            this.namedCache[name] = null
            return PlayerHeadFont.STEVE_HEAD
        }
        val component = future.get()
        this.uuidCache[uuid] = component
        this.namedCache[name] = component
        return component
    }

    private fun generateHeadAsync(skinTextureUrl: String): CompletableFuture<Component> {
        return CompletableFuture.supplyAsync {
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
                component
            } catch (e: IOException) {
                Arcade.logger.error("Failed to generate head texture from url: $skinTextureUrl", e)
                PlayerHeadFont.STEVE_HEAD
            }
        }
    }
}