/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.recorder.rejoin

import net.minecraft.network.Connection
import net.minecraft.network.protocol.PacketFlow

internal class RejoinConnection: Connection(PacketFlow.SERVERBOUND)