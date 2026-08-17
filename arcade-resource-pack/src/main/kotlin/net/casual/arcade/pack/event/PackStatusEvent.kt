/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.event

import com.mojang.authlib.GameProfile
import net.casual.arcade.events.common.ServerSideEvent
import net.casual.arcade.pack.PackStatus
import net.minecraft.server.MinecraftServer
import java.util.*

public data class PackStatusEvent(
    val server: MinecraftServer,
    val owner: GameProfile,
    val uuid: UUID,
    val status: PackStatus
): ServerSideEvent