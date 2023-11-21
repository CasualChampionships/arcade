package net.casual.arcade.stats

import net.casual.arcade.utils.json.DoubleSerializer
import net.casual.arcade.utils.json.FloatSerializer
import net.casual.arcade.utils.json.IntSerializer
import net.casual.arcade.utils.json.JsonSerializer
import net.minecraft.resources.ResourceLocation

public class StatType<T>(
    public val id: ResourceLocation,
    public val default: T,
    public val serializer: JsonSerializer<T>
) {
    public companion object {
        public fun int(id: ResourceLocation, default: Int = 0): StatType<Int> {
            return StatType(id, default, IntSerializer)
        }

        public fun float(id: ResourceLocation, default: Float = 0.0F): StatType<Float> {
            return StatType(id, default, FloatSerializer)
        }

        public fun double(id: ResourceLocation, default: Double = 0.0): StatType<Double> {
            return StatType(id, default, DoubleSerializer)
        }
    }
}