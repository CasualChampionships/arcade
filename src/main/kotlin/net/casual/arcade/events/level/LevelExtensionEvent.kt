package net.casual.arcade.events.level

import net.casual.arcade.events.core.ExtensionEvent
import net.casual.arcade.extensions.Extension
import net.casual.arcade.utils.LevelUtils.addExtension
import net.minecraft.server.level.ServerLevel

public data class LevelExtensionEvent(
    override val level: ServerLevel
): LevelEvent, ExtensionEvent {
    override fun addExtension(extension: Extension) {
        this.level.addExtension(extension)
    }
}