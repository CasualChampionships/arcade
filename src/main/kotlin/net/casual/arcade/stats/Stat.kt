package net.casual.arcade.stats

public class Stat<T>(
    public val stat: StatType<T>
) {
    public var value: T = this.stat.default
}