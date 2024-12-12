package net.casual.arcade.events.client

import net.casual.arcade.events.common.Event
import net.minecraft.client.Minecraft

public data class ClientStoppingEvent(
    val client: Minecraft
): Event
