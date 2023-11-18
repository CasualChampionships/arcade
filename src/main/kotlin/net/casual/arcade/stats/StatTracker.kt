package net.casual.arcade.stats

public class StatTracker {
    private val stats = HashMap<StatType<*>, Stat<*>>()

    public fun <T> getOrCreateStat(type: StatType<T>): Stat<T> {
        @Suppress("UNCHECKED_CAST")
        return this.stats.getOrPut(type) { Stat(type) } as Stat<T>
    }
}