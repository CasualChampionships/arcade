package net.casualuhc.arcade.scheduler

@Suppress("unused")
enum class MinecraftTimeUnit(
    private val ticks: Int
) {
    Ticks(1),
    Seconds(20),
    Minutes(1200);

    fun toTicks(time: Int): Int {
        return this.ticks * time
    }
}