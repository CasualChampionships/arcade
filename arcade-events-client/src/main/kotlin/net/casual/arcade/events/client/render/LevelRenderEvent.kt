package net.casual.arcade.events.client.render

import net.casual.arcade.events.common.Event
import net.minecraft.client.DeltaTracker
import net.minecraft.client.renderer.LevelRenderer

public data class LevelRenderEvent(
    val renderer: LevelRenderer,
    val deltas: DeltaTracker
): Event {
    public companion object {
        public const val TERRAIN: String = "terrain"
        public const val ENTITIES: String = "entities"
        public const val BLOCK_ENTITIES: String = "block-entities"
        public const val DEBUG: String = "debug"
        public const val TRANSLUCENT: String = "translucent"
    }
}