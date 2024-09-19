package net.casual.arcade.minigame.stats

import net.casual.arcade.utils.json.*
import net.minecraft.resources.ResourceLocation

public class StatType<T>(
    public val id: ResourceLocation,
    public val default: T,
    public val serializer: JsonSerializer<T>
) {
    public companion object {
        public fun bool(id: ResourceLocation, default: Boolean = false): StatType<Boolean> {
            return StatType(id, default, BooleanSerializer)
        }

        public fun int32(id: ResourceLocation, default: Int = 0): StatType<Int> {
            return StatType(id, default, IntSerializer)
        }

        public fun int64(id: ResourceLocation, default: Long = 0): StatType<Long> {
            return StatType(id, default, LongSerializer)
        }

        public fun float32(id: ResourceLocation, default: Float = 0.0F): StatType<Float> {
            return StatType(id, default, FloatSerializer)
        }

        public fun float64(id: ResourceLocation, default: Double = 0.0): StatType<Double> {
            return StatType(id, default, DoubleSerializer)
        }
    }
}