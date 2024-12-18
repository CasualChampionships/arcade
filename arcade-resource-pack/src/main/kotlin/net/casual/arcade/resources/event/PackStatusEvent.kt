/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.event

import com.mojang.authlib.GameProfile
import net.casual.arcade.events.common.Event
import net.casual.arcade.resources.pack.PackStatus
import net.minecraft.server.MinecraftServer
import java.util.*

public data class PackStatusEvent(
    val server: MinecraftServer,
    val owner: GameProfile,
    val uuid: UUID,
    val status: PackStatus
): Event