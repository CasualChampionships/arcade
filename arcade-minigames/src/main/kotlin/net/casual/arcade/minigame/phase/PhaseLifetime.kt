/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.phase

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.serialization.codec.setOf
import net.minecraft.util.ExtraCodecs

/**
 * Determines whether a scope survives a phase transition.
 *
 * @see net.casual.arcade.minigame.scope.MinigameScope
 */
public sealed interface PhaseLifetime {
    /**
     * Whether a scope with this lifetime survives a transition
     * from [previous] to [next].
     *
     * @param previous The phase being left.
     * @param next The phase being entered.
     * @return Whether the scope survives.
     */
    public fun survives(previous: Phase<Minigame>, next: Phase<Minigame>): Boolean

    public fun type(): Type

    /**
     * Survives every transition; only the minigame closing ends it.
     */
    public data object Forever: PhaseLifetime {
        override fun survives(previous: Phase<Minigame>, next: Phase<Minigame>): Boolean {
            return true
        }

        override fun type(): Type {
            return FOREVER
        }
    }

    public data object Current: PhaseLifetime {
        override fun survives(previous: Phase<Minigame>, next: Phase<Minigame>): Boolean {
            return false
        }

        override fun type(): Type {
            return CURRENT
        }
    }

    public data object Forward: PhaseLifetime {
        override fun survives(previous: Phase<Minigame>, next: Phase<Minigame>): Boolean {
            return next > previous
        }

        override fun type(): Type {
            return FORWARD
        }
    }

    public data class Until(public val bound: Phase<Minigame>): PhaseLifetime {
        override fun survives(previous: Phase<Minigame>, next: Phase<Minigame>): Boolean {
            return next < this.bound
        }

        override fun type(): Type {
            return UNTIL
        }
    }

    public data class During(public val phases: Set<Phase<Minigame>>): PhaseLifetime {
        override fun survives(previous: Phase<Minigame>, next: Phase<Minigame>): Boolean {
            return this.phases.contains(next)
        }

        override fun type(): Type {
            return DURING
        }
    }

    public fun interface Type {
        public fun codec(phase: Codec<Phase<Minigame>>): MapCodec<out PhaseLifetime>
    }

    public companion object {
        private val FOREVER: Type = Type { MapCodec.unit(Forever) }
        private val CURRENT: Type = Type { MapCodec.unit(Current) }
        private val FORWARD: Type = Type { MapCodec.unit(Forward) }
        private val UNTIL: Type = Type { phase ->
            phase.fieldOf("phase").xmap(::Until, Until::bound)
        }
        private val DURING: Type = Type { phase ->
            phase.setOf().fieldOf("phases").xmap(::During, During::phases)
        }

        private val TYPES: ExtraCodecs.LateBoundIdMapper<String, Type> = ExtraCodecs.LateBoundIdMapper()

        init {
            TYPES.put("forever", FOREVER)
            TYPES.put("current", CURRENT)
            TYPES.put("forward", FORWARD)
            TYPES.put("until", UNTIL)
            TYPES.put("during", DURING)
        }

        public fun codec(phase: Codec<Phase<Minigame>>): Codec<PhaseLifetime> {
            return TYPES.codec(Codec.STRING).dispatch("type", PhaseLifetime::type) { type -> type.codec(phase) }
        }
    }
}
