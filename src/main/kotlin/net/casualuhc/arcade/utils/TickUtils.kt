package net.casualuhc.arcade.utils

import net.casualuhc.arcade.Arcade
import java.lang.reflect.Field
import java.util.*

object TickUtils {
    @JvmStatic
    fun calculateMSPT(): Double {
        return Arrays.stream(Arcade.server.tickTimes).average().orElseThrow() * 1.0E-6
    }

    @JvmStatic
    fun calculateTPS(): Double {
        return 1000 / (if (this.isTickWarping()) 0.0 else this.getMSPT()).coerceAtLeast(this.calculateMSPT())
    }

    @JvmStatic
    fun isTickWarping(): Boolean {
        return this.start != null && this.start.getLong(null) != 0L
    }

    private fun getMSPT(): Double {
        if (this.milli != null) {
            return this.milli.getFloat(null).toDouble()
        }
        return 50.0
    }

    private val milli: Field?
    private val start: Field?

    init {
        var milli: Field? = null
        var start: Field? = null
        try {
            val clazz = Class.forName("carpet.helpers.TickSpeed")
            milli = clazz.getField("mspt")
            start = clazz.getField("tick_warp_start_time")
        } catch (_: ReflectiveOperationException) {

        }
        this.milli = milli
        this.start = start
    }
}