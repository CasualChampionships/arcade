/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.settings

import com.mojang.serialization.Codec
import net.casual.arcade.utils.serialization.codec.ArcadeExtraCodecs
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.resources.Identifier
import java.util.*

public class GameSettingType<T: Any>(
    public val codec: Codec<T>
) {
    public companion object {
        @JvmField
        public val BOOL: GameSettingType<Boolean> = GameSettingType(Codec.BOOL)

        @JvmField
        public val INT32: GameSettingType<Int> = GameSettingType(Codec.INT)

        @JvmField
        public val INT64: GameSettingType<Long> = GameSettingType(Codec.LONG)

        @JvmField
        public val FLOAT32: GameSettingType<Float> = GameSettingType(Codec.FLOAT)

        @JvmField
        public val FLOAT64: GameSettingType<Double> = GameSettingType(Codec.DOUBLE)

        @JvmField
        public val STRING: GameSettingType<String> = GameSettingType(Codec.STRING)

        @JvmField
        public val IDENTIFIER: GameSettingType<Identifier> = GameSettingType(Identifier.CODEC)

        @JvmField
        public val TIME: GameSettingType<MinecraftTimeDuration> = GameSettingType(MinecraftTimeDuration.CODEC)

        public inline fun <reified E: Enum<E>> enumeration(
            noinline mapper: (E) -> String = { it.name.lowercase() }
        ): GameSettingType<E> {
            return GameSettingType(ArcadeExtraCodecs.enum(mapper))
        }

        public inline fun <reified E: Enum<E>> optionalEnumeration(
            noinline mapper: (E) -> String = { it.name.lowercase() }
        ): GameSettingType<Optional<E>> {
            return GameSettingType(ArcadeExtraCodecs.optionalEnum(mapper))
        }
    }
}
