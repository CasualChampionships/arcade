/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.event

import net.casual.arcade.events.common.ServerSideEvent
import net.casual.arcade.pack.PackState
import java.util.*

public data class ClientPacksSuccessEvent(
    val uuid: UUID,
    val states: Collection<PackState>
): ServerSideEvent