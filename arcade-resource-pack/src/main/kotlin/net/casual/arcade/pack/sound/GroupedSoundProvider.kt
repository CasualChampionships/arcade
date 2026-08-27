/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.sound

import net.casual.arcade.pack.sound.SoundProvider.Type.Event
import net.casual.arcade.pack.sound.SoundProvider.Type.Sound
import net.minecraft.resources.Identifier

public class GroupedSoundProvider(private val attenuationDistance: Int) {
    private val providers = ArrayList<SoundProvider>()

    public fun sound(
        location: Identifier,
        volume: Float = 1.0F,
        pitch: Float = 1.0F,
        weight: Int = 1,
        stream: Boolean = false,
        attenuationDistance: Int = this.attenuationDistance,
        preload: Boolean = false
    ): GroupedSoundProvider {
        val provider = SoundProvider(location, volume, pitch, weight, stream, attenuationDistance, preload, Sound)
        this.providers.add(provider)
        return this
    }

    public fun event(
        location: Identifier,
        volume: Float = 1.0F,
        pitch: Float = 1.0F,
        weight: Int = 1,
        stream: Boolean = false,
        attenuationDistance: Int = this.attenuationDistance,
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