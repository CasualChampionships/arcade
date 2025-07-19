/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.font.heads

import com.google.common.cache.CacheBuilder
import com.mojang.authlib.GameProfile
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.casual.arcade.resources.ArcadeResourcePacks
import net.casual.arcade.resources.font.pixel.PixelFontResources
import net.casual.arcade.resources.font.spacing.SpacingFontResources
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.ComponentUtils.color
import net.casual.arcade.utils.ComponentUtils.wrap
import net.casual.arcade.utils.PlayerUtils.levelServer
import net.casual.arcade.utils.PlayerUtils.player
import net.casual.arcade.utils.ServerUtils
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.awt.Color
import java.io.IOException
import java.net.URI
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.io.path.inputStream

public class PlayerHeadComponents(private val shift: Int) {
    private val uuidCache = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .build<UUID, CompletableFuture<Component>>()

    private val nameCache = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .build<String, CompletableFuture<Component>>()

    private val invalidNames = ConcurrentHashMap.newKeySet<String>()

    private val steve by lazy(this::createSteveHead)

    public fun getDefault(): Component {
        return this.steve
    }

    public fun getHeadOrDefault(player: ServerPlayer): Component {
        return getHead(player).getNow(this.steve)
    }

    public fun getHeadOrDefault(name: String, server: MinecraftServer = ServerUtils.getServer()): Component {
        return getHead(name, server).getNow(this.steve)
    }

    public fun getHead(
        player: ServerPlayer,
        force: Boolean = false
    ): CompletableFuture<Component> {
        val uuid = player.uuid
        val name = player.scoreboardName
        val existing = this.uuidCache.getIfPresent(uuid)
        if (!force && existing != null) {
            return existing
        }
        val skinUrl = this.getSkinUrl(player.gameProfile, player.levelServer)
            ?: return CompletableFuture.completedFuture(this.steve)
        val future = CompletableFuture.supplyAsync {
            val component = generateHead(skinUrl)
            if (existing != null) {
                this.cacheHead(name, uuid, CompletableFuture.completedFuture(component))
            }
            component
        }
        if (existing == null) {
            this.cacheHead(name, uuid, future)
        }
        return future
    }

    public fun getHead(
        name: String,
        server: MinecraftServer = ServerUtils.getServer(),
        force: Boolean = false
    ): CompletableFuture<Component> {
        if (this.invalidNames.contains(name)) {
            return CompletableFuture.completedFuture(this.steve)
        }
        val existing = this.nameCache.getIfPresent(name)
        if (!force && existing != null) {
            return existing
        }

        val player = server.player(name)
        if (player != null) {
            // This is faster since we don't have to look up uuid
            return getHead(player, force)
        }

        val future = server.profileCache!!.getAsync(name).thenApply { optional ->
            if (optional.isEmpty) {
                invalidNames.add(name)
                return@thenApply this.steve
            }
            val uuid = optional.get().id
            // If they're in the uuid cache, they should've been
            // in the nameCache too, but we might as well check...
            val cached = this.uuidCache.getIfPresent(uuid)
            if (!force && cached != null) {
                return@thenApply cached.join()
            }

            // The previous profile didn't have textures
            val profile = server.sessionService.fetchProfile(uuid, true)?.profile
                ?: return@thenApply this.steve
            val skinUrl = this.getSkinUrl(profile, server) ?: return@thenApply this.steve
            val component = generateHead(skinUrl)
            if (existing != null) {
                this.cacheHead(name, uuid, CompletableFuture.completedFuture(component))
            }
            component
        }
        if (existing == null) {
            this.nameCache.put(name, future)
        }
        return future
    }


    public fun invalidateHead(player: ServerPlayer) {
        this.uuidCache.invalidate(player.uuid)
        this.nameCache.invalidate(player.scoreboardName)
    }

    private fun cacheHead(name: String, uuid: UUID, future: CompletableFuture<Component>) {
        this.nameCache.put(name, future)
        this.uuidCache.put(uuid, future)
    }

    private fun getSkinUrl(profile: GameProfile, server: MinecraftServer): String? {
        return server.sessionService.getTextures(profile).skin?.url
    }

    private fun createSteveHead(): Component {
        try {
            val path = ArcadeResourcePacks.path("packs/PlayerHeads/steve.png")
            val image = path.inputStream().use(ImageIO::read)
            val transparent = Color(0, true)
            return this.convertImageToComponent(
                { x, y -> Color(image.getRGB(x, y), true) },
                { _, _ -> transparent }
            )
        } catch (e: IOException) {
            return Component.empty()
        }
    }

    private fun generateHead(skinTextureUrl: String): Component {
        try {
            val image = ImageIO.read(URI(skinTextureUrl).toURL())
            return this.convertImageToComponent(
                { x, y -> Color(image.getRGB(x + 40, y + 8)) },
                { x, y -> Color(image.getRGB(x + 8, y + 8), true) }
            )
        } catch (e: IOException) {
            ArcadeUtils.logger.error("Failed to generate head texture from url: $skinTextureUrl", e)
            return this.steve
        }
    }

    private inline fun convertImageToComponent(
        baseFetcher: (x: Int, y: Int) -> Color,
        hatFetcher: (x: Int, y: Int) -> Color
    ): MutableComponent {
        val component = Component.empty()
        for (y in 0..7) {
            for (x in 0..7) {
                if (x != 0) {
                    component.append(SpacingFontResources.spaced(-1))
                }
                val hat = hatFetcher.invoke(x, y)
                val base = baseFetcher.invoke(x, y)
                val pixel = PixelFontResources.pixel(8 - y + this.shift).wrap().color(base.overlayWith(hat).rgb)
                component.append(pixel)
            }
            if (y != 7) {
                component.append(SpacingFontResources.spaced(-9))
            }
        }
        return component
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

    public companion object {
        private val components = Int2ObjectOpenHashMap<PlayerHeadComponents>()

        public fun get(shift: Int = 0): PlayerHeadComponents {
            return this.components.computeIfAbsent(shift, Int2ObjectFunction(::PlayerHeadComponents))
        }

        public fun getHeadOrDefault(player: ServerPlayer, shift: Int = 0): Component {
            return this.get(shift).getHeadOrDefault(player)
        }

        public fun getHeadOrDefault(
            name: String,
            server: MinecraftServer = ServerUtils.getServer(),
            shift: Int = 0
        ): Component {
            return this.get(shift).getHeadOrDefault(name, server)
        }
    }
}