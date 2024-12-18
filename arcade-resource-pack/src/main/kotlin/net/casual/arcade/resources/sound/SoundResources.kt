/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.sound

import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import net.casual.arcade.resources.sound.SoundProvider.Type.Event
import net.casual.arcade.resources.sound.SoundProvider.Type.Sound
import net.casual.arcade.utils.JsonUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent

public abstract class SoundResources(
    public val namespace: String
) {
    private val providers = Object2ObjectLinkedOpenHashMap<String, List<SoundProvider>>()

    protected fun sound(
        location: ResourceLocation,
        volume: Float = 1.0F,
        pitch: Float = 1.0F,
        stream: Boolean = false,
        attenuationDistance: Int = 16,
        dynamicRange: Boolean = false,
        preload: Boolean = false,
        id: String = location.path
    ): SoundEvent {
        val provider = SoundProvider(location, volume, pitch, 1, stream, attenuationDistance, preload, Sound)
        this.providers[id] = listOf(provider)
        return this.register(ResourceLocation.fromNamespaceAndPath(this.namespace, id), attenuationDistance, dynamicRange)
    }

    protected fun event(
        location: ResourceLocation,
        volume: Float = 1.0F,
        pitch: Float = 1.0F,
        stream: Boolean = false,
        attenuationDistance: Int = 16,
        dynamicRange: Boolean = false,
        preload: Boolean = false,
        id: String = location.path
    ): SoundEvent {
        val provider = SoundProvider(location, volume, pitch, 1, stream, attenuationDistance, preload, Event)
        this.providers[id] = listOf(provider)
        return this.register(ResourceLocation.fromNamespaceAndPath(this.namespace, id), attenuationDistance, dynamicRange)
    }

    protected fun group(
        id: String,
        attenuationDistance: Int = 16,
        dynamicRange: Boolean = false,
        builder: GroupedSoundProvider.() -> Unit
    ): SoundEvent {
        val grouped = GroupedSoundProvider()
        grouped.builder()
        this.providers[id] = grouped.getProviders()
        return this.register(ResourceLocation.fromNamespaceAndPath(this.namespace, id), attenuationDistance, dynamicRange)
    }

    protected fun at(path: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(this.namespace, path)
    }

    internal fun toJson(): String {
        val code = SoundProvider.CODEC.listOf()
        val json = JsonObject()
        for ((key, providers) in this.providers) {
            val group = JsonObject()
            val result = code.encodeStart(JsonOps.INSTANCE, providers).orThrow
            group.add("sounds", result)
            json.add(key, group)
        }
        return JsonUtils.MIN_GSON.toJson(json)
    }

    private fun register(id: ResourceLocation, distance: Int, dynamicRange: Boolean): SoundEvent {
        val sound = if (dynamicRange) {
            SoundEvent.createVariableRangeEvent(id)
        } else {
            SoundEvent.createFixedRangeEvent(id, distance.toFloat())
        }
        return sound
    }
}