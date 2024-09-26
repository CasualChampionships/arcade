package net.casual.arcade.dimensions.level

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.level.dimension.LevelStem

public class LevelGenerationOptions(
    public val stem: LevelStem,
    public val seed: Long,
    public val flat: Boolean,
    public val tickTime: Boolean,
    public val generateStructures: Boolean,
    public val debug: Boolean,
) {
    public companion object {
        public val CODEC: Codec<LevelGenerationOptions> = RecordCodecBuilder.create { instance ->
            instance.group(
                LevelStem.CODEC.fieldOf("stem").forGetter(LevelGenerationOptions::stem),
                Codec.LONG.fieldOf("seed").forGetter(LevelGenerationOptions::seed),
                Codec.BOOL.fieldOf("flat").forGetter(LevelGenerationOptions::flat),
                Codec.BOOL.fieldOf("tick_time").forGetter(LevelGenerationOptions::tickTime),
                Codec.BOOL.fieldOf("generate_structures").forGetter(LevelGenerationOptions::generateStructures),
                Codec.BOOL.fieldOf("debug").forGetter(LevelGenerationOptions::debug)
            ).apply(instance, ::LevelGenerationOptions)
        }
    }
}