/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.font.heads

import com.google.common.cache.CacheBuilder
import com.mojang.authlib.minecraft.MinecraftSessionService
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import net.casual.arcade.pack.ArcadeResourcePacks
import net.casual.arcade.pack.font.pixel.PixelFontResources
import net.casual.arcade.pack.font.spacing.SpacingFontResources
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.component.color
import net.casual.arcade.utils.component.wrap
import net.casual.arcade.utils.coroutine.getNowOrNull
import net.casual.arcade.utils.player.resolveProfileOrNull
import net.casual.arcade.utils.player.server
import net.casual.arcade.utils.player.uuid
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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val uuidCache = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .build<UUID, Deferred<Result>>()

    private val nameCache = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .build<String, Deferred<Result>>()

    private val steve by lazy(this::createSteveHead)

    public override fun getDefault(): Component {
        return this.steve
    }

    override fun getHeadOrDefaultFor(resolvable: ResolvableProfile): Component {
        val username = resolvable.name().getOrNull()
        val uuid = resolvable.uuid().getOrNull()
        if (username != null) {
            val result = this.nameCache.getIfPresent(username)
            if (result != null) {
                return result.getNowOrNull()?.getOrNull() ?: return this.getDefault()
            }
        }

        if (uuid != null) {
            val result = this.uuidCache.getIfPresent(uuid)
            if (result != null) {
                return result.getNowOrNull()?.getOrNull() ?: return this.getDefault()
            }
        }

        @Suppress("DeferredResultUnused")
        this.initializeHeadCache(resolvable, username, uuid)
        return getDefault()
    }

    override suspend fun getHeadFor(resolvable: ResolvableProfile): Component {
        return this.getHeadFor(resolvable, false)
    }

    @Suppress("DeferredResultUnused")
    public suspend fun getHeadFor(resolvable: ResolvableProfile, force: Boolean = false): Component {
        val username = resolvable.name().getOrNull()
        val uuid = resolvable.uuid().getOrNull()

        if (username == null && uuid == null) {
            return this.getDefault()
        }

        if (force) {
            if (username != null) {
                this.nameCache.invalidate(username)
            }
            if (uuid != null) {
                this.uuidCache.invalidate(uuid)
            }
        } else {
            var existing: Deferred<Result>? = null
            if (username != null) {
                existing = this.nameCache.getIfPresent(username)
            }
            if (existing == null && uuid != null) {
                existing = this.uuidCache.getIfPresent(uuid)
            }

            if (existing != null) {
                return when (val result = existing.await()) {
                    is Success -> result.component
                    is Invalid -> this.getDefault()
                }
            }
        }

        val deferred = this.initializeHeadCache(resolvable, username, uuid)
        if (uuid != null) {
            this.uuidCache.asMap().putIfAbsent(uuid, deferred)
        }
        if (username != null) {
            this.nameCache.asMap().putIfAbsent(username, deferred)
        }

        return when (val result = deferred.await()) {
            is Success -> result.component
            is Invalid -> this.getDefault()
        }
    }

    private fun initializeHeadCache(resolvable: ResolvableProfile, username: String?, uuid: UUID?): Deferred<Result> {
        return if (uuid != null) {
            this.uuidCache.get(uuid) { loadHead(resolvable) }
        } else if (username != null) {
            this.nameCache.get(username) { loadHead(resolvable) }
        } else {
            CompletableDeferred(Invalid)
        }
    }

    @Suppress("DeferredResultUnused")
    private fun loadHead(resolvable: ResolvableProfile): CompletableDeferred<Result> {
        val task = CompletableDeferred<Result>()
        this.scope.launch {
            try {
                val resolved = resolvable.resolveProfileOrNull(resolver).await()
                if (resolved == null) {
                    task.complete(Invalid)
                    return@launch
                }

                uuidCache.asMap().putIfAbsent(resolved.id, task)
                nameCache.asMap().putIfAbsent(resolved.name, task)

                val url = session.getTextures(resolved).skin?.url
                if (url == null) {
                    task.complete(Invalid)
                    return@launch
                }

                val component = generateHead(url)
                task.complete(Success(component))
            } catch (_: Exception) {
                task.complete(Invalid)
            }
        }
        return task
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
        } catch (_: IOException) {
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

    private fun shutdown() {
        this.scope.cancel()
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

    private sealed interface Result {
        fun getOrNull(): Component?
    }

    private data object Invalid: Result {
        override fun getOrNull(): Component? {
            return null
        }
    }

    private data class Success(val component: Component): Result {
        override fun getOrNull(): Component {
            return this.component
        }
    }

    public companion object {
        private val components = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .removalListener<Int, PixelGridHeadComponents> { notification -> notification.value?.shutdown() }
            .build<Int, PixelGridHeadComponents>()

        public fun get(shift: Int = 0, services: Services): PixelGridHeadComponents {
            return this.get(shift, services.profileResolver, services.sessionService)
        }

        public fun get(
            shift: Int = 0,
            resolver: ProfileResolver,
            session: MinecraftSessionService
        ): PixelGridHeadComponents {
            return this.components.get(shift) {
                PixelGridHeadComponents(shift, resolver, session)
            }
        }

        public fun getHeadOrDefaultFor(player: ServerPlayer, shift: Int = 0): Component {
            val server = player.server
            val services = server.services()
            val components = this.get(shift, services.profileResolver, services.sessionService)
            return components.getHeadOrDefaultFor(player)
        }

        public fun getHeadOrDefaultFor(
            resolvable: ResolvableProfile,
            server: MinecraftServer,
            shift: Int = 0
        ): Component {
            val services = server.services()
            val components = this.get(shift, services.profileResolver, services.sessionService)
            return components.getHeadOrDefaultFor(resolvable)
        }
    }
}