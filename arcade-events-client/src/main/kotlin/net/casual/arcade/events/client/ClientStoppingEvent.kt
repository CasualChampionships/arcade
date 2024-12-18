/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client

import net.casual.arcade.events.common.Event
import net.minecraft.client.Minecraft

public data class ClientStoppingEvent(
    val client: Minecraft
): Event
