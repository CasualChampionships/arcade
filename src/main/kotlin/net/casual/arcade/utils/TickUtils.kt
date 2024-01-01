package net.casual.arcade.utils

import net.casual.arcade.Arcade
import net.minecraft.util.TimeUtil
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

public object TickUtils {
    @JvmStatic
    public fun calculateMSPT(): Double {
        return TimeUnit.NANOSECONDS.toMillis(Arcade.getServer().averageTickTimeNanos).toDouble()
    }

    @JvmStatic
    public fun calculateTPS(): Double {
        val manager = Arcade.getServer().tickRateManager()
        val mspt = this.calculateMSPT()
        return 1000 / if (manager.isSprinting) mspt else max(mspt, manager.millisecondsPerTick().toDouble())
    }
}