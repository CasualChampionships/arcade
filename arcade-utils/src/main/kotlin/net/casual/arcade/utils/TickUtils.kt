/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.minecraft.server.MinecraftServer
import net.minecraft.util.TimeUtil
import kotlin.math.max

public fun MinecraftServer.calculateMSPT(): Float {
    return this.averageTickTimeNanos.toFloat() / TimeUtil.NANOSECONDS_PER_MILLISECOND
}

public fun MinecraftServer.calculateTPS(): Float {
    val manager = this.tickRateManager()
    val mspt = this.calculateMSPT()
    return 1000 / if (manager.isSprinting) mspt else max(mspt, manager.millisecondsPerTick())
}