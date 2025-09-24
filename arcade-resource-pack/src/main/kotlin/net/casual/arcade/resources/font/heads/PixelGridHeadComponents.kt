/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.font.heads

import com.google.common.cache.CacheBuilder
import com.mojang.authlib.minecraft.MinecraftSessionService
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import net.casual.arcade.resources.ArcadeResourcePacks
import net.casual.arcade.resources.font.pixel.PixelFontResources
import net.casual.arcade.resources.font.spacing.SpacingFontResources
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.PlayerUtils.levelServer
import net.casual.arcade.utils.component.color
import net.casual.arcade.utils.component.wrap
import net.casual.arcade.utils.coroutine.async
import net.casual.arcade.utils.coroutine.getNow
import net.casual.arcade.utils.resolveProfileOrNull
import net.casual.arcade.utils.uuid
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.Services
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.players.ProfileResolver
import net.minecraft.world.item.component.ResolvableProfile
import java.awt.Color
import java.io.IOException
import java.net.URI
import java.util.*
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.io.path.inputStream
import kotlin.jvm.optionals.getOrNull

public class PixelGridHeadComponents private constructor(
    private val shift: Int,
    private val resolver: ProfileResolver,
    private val session: MinecraftSessionService
): TexturedHeadComponents {
    private val uuidCache = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .build<UUID, Result>()

    private val nameCache = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .build<String, Result>()

    private val steve by lazy(this::createSteveHead)

    public override fun getDefault(): Component {
        return this.steve
    }

    override suspend fun getHeadFor(resolvable: ResolvableProfile): Component {
        return this.getHeadFor(resolvable, false)
    }

    public suspend fun getHeadFor(resolvable: ResolvableProfile, force: Boolean = false): Component {
        val username = resolvable.name().getOrNull()
        val uuid = resolvable.uuid().getOrNull()
        if (username != null) {
            when (val existing = this.nameCache.getIfPresent(username)) {
                is Success -> if (!force) return existing.component.await()
                is Invalid -> return this.getDefault()
            }
        }
        if (uuid != null) {
            when (val existing = this.uuidCache.getIfPresent(uuid)) {
                is Success -> if (!force) return existing.component.await()
                is Invalid -> return this.getDefault()
            }
        }

        val resolved = resolvable.resolveProfileOrNull(this.resolver).await()
        if (resolved == null) {
            if (username != null) {
                this.nameCache.put(username, Invalid)
            }
            if (uuid != null) {
                this.uuidCache.put(uuid, Invalid)
            }
            return this.getDefault()
        }

        val url = this.session.getTextures(resolved).skin?.url
            ?: return this.getDefault()
        val deferred = withContext(Dispatchers.IO) {
            async { generateHead(url) }
        }
        val success = Success(deferred)
        this.nameCache.put(resolved.name, success)
        this.uuidCache.put(resolved.id, success)
        return deferred.await()
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
                { x, y -> Color(image.getRGB(x + 8, y + 8), true) },
                { x, y -> Color(image.getRGB(x + 40, y + 8), true) }
            )
        } catch (e: IOException) {
            ArcadeUtils.logger.error("Failed to generate head texture from url: $skinTextureUrl", e)
            return this.getDefault()
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

    private interface Result

    private data object Invalid: Result

    private data class Success(val component: Deferred<Component>): Result

    public companion object {
        private val components = WeakHashMap<Int, PixelGridHeadComponents>()

        public fun get(shift: Int = 0, services: Services): PixelGridHeadComponents {
            return this.get(shift, services.profileResolver, services.sessionService)
        }

        public fun get(
            shift: Int = 0,
            resolver: ProfileResolver,
            session: MinecraftSessionService
        ): PixelGridHeadComponents {
            return this.components.computeIfAbsent(shift, Int2ObjectFunction { s ->
                PixelGridHeadComponents(s, resolver, session)
            })
        }

        public fun getHeadOrDefaultFor(player: ServerPlayer, shift: Int = 0): Component {
            val server = player.levelServer
            val services = server.services()
            val components = this.get(shift, services.profileResolver, services.sessionService)
            val deferred = server.async { components.getHeadFor(player) }
            return deferred.getNow(components.getDefault())
        }

        public fun getHeadOrDefaultFor(
            resolvable: ResolvableProfile,
            server: MinecraftServer,
            shift: Int = 0
        ): Component {
            val services = server.services()
            val components = this.get(shift, services.profileResolver, services.sessionService)
            val deferred = server.async { components.getHeadFor(resolvable) }
            return deferred.getNow(components.getDefault())
        }
    }
}