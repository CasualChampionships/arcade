package net.casual.arcade.stats

import net.casual.arcade.utils.json.JsonSerializer
import net.minecraft.resources.ResourceLocation

public class StatType<T>(
    public val id: ResourceLocation,
    public val default: T,
    public val serializer: JsonSerializer<T>
)