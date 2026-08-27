/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.sound

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mojang.serialization.JsonOps
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import net.casual.arcade.pack.sound.SoundProvider.Type.Event
import net.casual.arcade.pack.sound.SoundProvider.Type.Sound
import net.casual.arcade.utils.Identifier
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent
import org.jetbrains.annotations.ApiStatus.Internal

public abstract class SoundResources(
    public val namespace: String
) {
    private val providers = Object2ObjectLinkedOpenHashMap<String, SubtitledProviders>()

    protected fun sound(
        location: Identifier,
        volume: Float = 1.0F,
        pitch: Float = 1.0F,
        stream: Boolean = false,
        attenuationDistance: Int = 16,
        dynamicRange: Boolean = false,
        preload: Boolean = false,
        subtitle: String? = null,
        id: String = location.path
    ): SoundEvent {
        val provider = SoundProvider(location, volume, pitch, 1, stream, attenuationDistance, preload, Sound)
        this.providers[id] = SubtitledProviders(subtitle, listOf(provider))
        return this.register(Identifier(this.namespace, id), attenuationDistance, dynamicRange)
    }

    protected fun event(
        location: Identifier,
        volume: Float = 1.0F,
        pitch: Float = 1.0F,
        stream: Boolean = false,
        attenuationDistance: Int = 16,
        dynamicRange: Boolean = false,
        preload: Boolean = false,
        subtitle: String? = null,
        id: String = location.path
    ): SoundEvent {
        val provider = SoundProvider(location, volume, pitch, 1, stream, attenuationDistance, preload, Event)
        this.providers[id] = SubtitledProviders(subtitle, listOf(provider))
        return this.register(Identifier(this.namespace, id), attenuationDistance, dynamicRange)
    }

    protected fun group(
        id: String,
        attenuationDistance: Int = 16,
        dynamicRange: Boolean = false,
        subtitle: String? = null,
        builder: GroupedSoundProvider.() -> Unit
    ): SoundEvent {
        val grouped = GroupedSoundProvider(attenuationDistance)
        grouped.builder()
        this.providers[id] = SubtitledProviders(subtitle, grouped.getProviders())
        return this.register(Identifier(this.namespace, id), attenuationDistance, dynamicRange)
    }

    protected fun at(path: String): Identifier {
        return Identifier(this.namespace, path)
    }

    @Internal
    public fun toJson(): JsonObject {
        val code = SoundProvider.CODEC.listOf()
        val json = JsonObject()
        for ((key, subtitled) in this.providers) {
            val (subtitle, providers) = subtitled
            val group = JsonObject()
            val result = code.encodeStart(JsonOps.INSTANCE, providers).orThrow
            if (subtitle != null) {
                group.add("subtitle", JsonPrimitive(subtitle))
            }
            group.add("sounds", result)
            json.add(key, group)
        }
        return json
    }

    private fun register(id: Identifier, distance: Int, dynamicRange: Boolean): SoundEvent {
        val sound = if (dynamicRange) {
            SoundEvent.createVariableRangeEvent(id)
        } else {
            SoundEvent.createFixedRangeEvent(id, distance.toFloat())
        }
        return sound
    }

    private data class SubtitledProviders(val subtitle: String?, val providers: List<SoundProvider>)
}