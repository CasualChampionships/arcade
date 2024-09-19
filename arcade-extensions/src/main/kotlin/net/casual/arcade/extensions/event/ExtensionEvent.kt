package net.casual.arcade.extensions.event

import net.casual.arcade.events.core.Event
import net.casual.arcade.extensions.Extension

public interface ExtensionEvent: Event {
    public fun addExtension(extension: Extension)
}