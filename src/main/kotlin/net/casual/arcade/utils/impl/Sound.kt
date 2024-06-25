package net.casual.arcade.utils.impl

import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource

public data class Sound(
    val sound: SoundEvent,
    val source: SoundSource = SoundSource.MASTER,
    val volume: Float = 1.0F,
    val pitch: Float = 1.0F,
    // Whether the sound will change volume based on how far the player moves
    // If true - always plays the same volume
    val static: Boolean = false
)
