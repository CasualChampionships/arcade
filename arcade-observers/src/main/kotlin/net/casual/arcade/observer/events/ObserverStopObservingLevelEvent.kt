/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.observer.events

import net.casual.arcade.events.common.ServerSideEvent
import net.casual.arcade.observer.Observer
import net.minecraft.server.level.ServerLevel

public data class ObserverStopObservingLevelEvent(
    val observer: Observer,
    val level: ServerLevel
): ServerSideEvent