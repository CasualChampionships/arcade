package net.casual.arcade.utils

import net.casual.arcade.Arcade
import java.util.*

object TickUtils {
    @JvmStatic
    fun calculateMSPT(): Double {
        return Arrays.stream(Arcade.getServer().tickTimes).average().orElseThrow() * 1.0E-6
    }

    @JvmStatic
    fun calculateTPS(): Double {
        return 1000 / this.calculateMSPT().coerceAtLeast(50.0)
    }
}