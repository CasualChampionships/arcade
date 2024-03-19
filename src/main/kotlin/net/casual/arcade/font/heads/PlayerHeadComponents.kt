package net.casual.arcade.font.heads

import com.mojang.authlib.minecraft.MinecraftProfileTexture
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.colour
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO

public object PlayerHeadComponents {
    private val futures = HashMap<UUID, CompletableFuture<Component>>()
    private val cache = HashMap<UUID, Component?>()

    public fun getHeadFor(player: ServerPlayer): Component {
        val uuid = player.uuid
        if (this.cache.containsKey(uuid)) {
            return this.cache[uuid] ?: PlayerHeadFont.STEVE_HEAD
        }
        return this.generateHead(player)
    }

    private fun generateHead(player: ServerPlayer): Component {
        val uuid = player.uuid
        val future = this.futures[uuid]
        if (future == null) {
            val textures = player.server.sessionService.getTextures(player.gameProfile)
            val skinTexture = textures.skin
            if (skinTexture == null) {
                this.cache[uuid] = null
                return PlayerHeadFont.STEVE_HEAD
            }
            this.futures[uuid] = this.generateHeadAsync(skinTexture)
            return PlayerHeadFont.STEVE_HEAD
        }

        if (!future.isDone) {
            return PlayerHeadFont.STEVE_HEAD
        }
        this.futures.remove(uuid)
        if (future.isCompletedExceptionally || future.isCancelled) {
            this.cache[uuid] = null
            return PlayerHeadFont.STEVE_HEAD
        }
        val component = future.get()
        this.cache[uuid] = component
        return component
    }

    private fun generateHeadAsync(skinTexture: MinecraftProfileTexture): CompletableFuture<Component> {
        return CompletableFuture.supplyAsync {
            val image = ImageIO.read(URL(skinTexture.url))
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
        }
    }
}