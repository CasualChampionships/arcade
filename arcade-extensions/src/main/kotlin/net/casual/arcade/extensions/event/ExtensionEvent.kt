/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.event

import net.casual.arcade.events.common.Event
import net.casual.arcade.extensions.Extension

public interface ExtensionEvent: Event {
    public fun addExtension(extension: Extension)
}