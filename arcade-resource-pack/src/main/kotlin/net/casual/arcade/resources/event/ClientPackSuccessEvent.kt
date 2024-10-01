package net.casual.arcade.resources.event

import net.casual.arcade.events.core.Event
import net.casual.arcade.resources.pack.PackState
import java.util.UUID

public data class ClientPackSuccessEvent(
    val uuid: UUID,
    val states: Collection<PackState>
): Event