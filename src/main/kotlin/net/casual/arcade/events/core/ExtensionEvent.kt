package net.casual.arcade.events.core

import net.casual.arcade.extensions.Extension

public interface ExtensionEvent: Event {
    public fun addExtension(extension: Extension)
}