package net.casual.arcade.utils

import net.casual.arcade.Arcade
import java.util.*

public object TickUtils {
    @JvmStatic
    public fun calculateMSPT(): Double {
        return Arrays.stream(Arcade.getServer().tickTimes).average().orElseThrow() * 1.0E-6
    }

    @JvmStatic
    public fun calculateTPS(): Double {
        return 1000 / this.calculateMSPT().coerceAtLeast(50.0)
    }
}