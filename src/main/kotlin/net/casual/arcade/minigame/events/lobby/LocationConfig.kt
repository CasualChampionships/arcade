package net.casual.arcade.minigame.events.lobby

import kotlinx.serialization.Serializable

@Serializable
public class LocationConfig(
    public val x: Double = 0.0,
    public val y: Double = 1.0,
    public val z: Double = 0.0,
    public val yaw: Float = 0.0F,
    public val pitch: Float = 0.0F
)