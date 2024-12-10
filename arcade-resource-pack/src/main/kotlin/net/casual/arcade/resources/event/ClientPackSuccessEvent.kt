package net.casual.arcade.resources.event

import net.casual.arcade.events.common.Event
import net.casual.arcade.resources.pack.PackState
import java.util.*

public data class ClientPackSuccessEvent(
    val uuid: UUID,
    val states: Collection<PackState>
): Event