/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.sound

import net.casual.arcade.resources.sound.SoundProvider.Type.Event
import net.casual.arcade.resources.sound.SoundProvider.Type.Sound
import net.minecraft.resources.ResourceLocation

public class GroupedSoundProvider {
    private val providers = ArrayList<SoundProvider>()

    public fun sound(
        location: ResourceLocation,
        volume: Float = 1.0F,
        pitch: Float = 1.0F,
        weight: Int = 1,
        stream: Boolean = false,
        attenuationDistance: Int = 16,
        preload: Boolean = false
    ): GroupedSoundProvider {
        val provider = SoundProvider(location, volume, pitch, weight, stream, attenuationDistance, preload, Sound)
        this.providers.add(provider)
        return this
    }

    public fun event(
        location: ResourceLocation,
        volume: Float = 1.0F,
        pitch: Float = 1.0F,
        weight: Int = 1,
        stream: Boolean = false,
        attenuationDistance: Int = 16,
        preload: Boolean = false
    ): GroupedSoundProvider {
        val provider = SoundProvider(location, volume, pitch, weight, stream, attenuationDistance, preload, Event)
        this.providers.add(provider)
        return this
    }

    internal fun getProviders(): List<SoundProvider> {
        return this.providers
    }
}