package net.casual.arcade.utils.impl

import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource

public data class Sound(
    val sound: SoundEvent,
    val source: SoundSource = SoundSource.MASTER,
    val volume: Float = 1.0F,
    val pitch: Float = 1.0F
)
