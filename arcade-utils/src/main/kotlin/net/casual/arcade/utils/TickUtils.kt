package net.casual.arcade.utils

import net.minecraft.server.MinecraftServer
import java.util.concurrent.TimeUnit
import kotlin.math.max

public fun MinecraftServer.calculateMSPT(): Double {
    return TimeUnit.NANOSECONDS.toMillis(this.averageTickTimeNanos).toDouble()
}

public fun MinecraftServer.calculateTPS(): Double {
    val manager = this.tickRateManager()
    val mspt = this.calculateMSPT()
    return 1000 / if (manager.isSprinting) mspt else max(mspt, manager.millisecondsPerTick().toDouble())
}