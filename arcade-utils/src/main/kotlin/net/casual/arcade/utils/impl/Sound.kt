/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.impl

import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource

public data class Sound(
    val event: SoundEvent,
    val source: SoundSource = SoundSource.MASTER,
    val volume: Float = 1.0F,
    val pitch: Float = 1.0F,
    // Whether the sound will change volume based on how far the player moves
    // If true - always plays the same volume
    val static: Boolean = false
)

public data class TimedSound(
    val sound: Sound,
    val duration: MinecraftTimeDuration
)