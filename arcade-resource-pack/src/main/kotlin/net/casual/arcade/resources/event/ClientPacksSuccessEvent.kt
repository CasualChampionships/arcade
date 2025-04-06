/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.event

import net.casual.arcade.events.common.Event
import net.casual.arcade.resources.pack.PackState
import java.util.*

public data class ClientPacksSuccessEvent(
    val uuid: UUID,
    val states: Collection<PackState>
): Event