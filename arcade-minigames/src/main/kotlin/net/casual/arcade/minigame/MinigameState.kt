/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.phase.MinigamePhase
import net.minecraft.util.ExtraCodecs

public sealed interface MinigameState {
    public fun isAt(phase: MinigamePhase): Boolean {
        return (this as? Playing)?.phase == phase
    }

    public operator fun compareTo(phase: MinigamePhase): Int {
        val current = (this as? Playing)?.phase ?: return -1
        return current.compareTo(phase)
    }

    public fun type(): Type

    public data object Created: MinigameState {
        override fun type(): Type {
            return CREATED
        }
    }

    public data object Ready: MinigameState {
        override fun type(): Type {
            return READY
        }
    }

    public data class Playing(public val phase: MinigamePhase): MinigameState {
        override fun type(): Type {
            return PLAYING
        }
    }

    public data class Closed(public val completed: Boolean): MinigameState {
        override fun type(): Type {
            return CLOSED
        }
    }

    public fun interface Type {
        public fun codec(phase: Codec<MinigamePhase>): MapCodec<out MinigameState>
    }

    public companion object {
        private val CREATED = Type { MapCodec.unit(Created) }
        private val READY = Type { MapCodec.unit(Ready) }
        private val PLAYING = Type { phase -> phase.fieldOf("phase").xmap(::Playing, Playing::phase) }
        private val CLOSED = Type { Codec.BOOL.fieldOf("completed").xmap(::Closed, Closed::completed) }

        private val TYPES = ExtraCodecs.LateBoundIdMapper<String, Type>()

        init {
            TYPES.put("created", CREATED)
            TYPES.put("ready", READY)
            TYPES.put("playing", PLAYING)
            TYPES.put("closed", CLOSED)
        }

        public fun codec(phases: Codec<MinigamePhase>): Codec<MinigameState> {
            return TYPES.codec(Codec.STRING).dispatch(MinigameState::type) { type -> type.codec(phases) }
        }
    }
}
