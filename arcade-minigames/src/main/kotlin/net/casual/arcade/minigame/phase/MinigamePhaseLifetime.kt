/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.phase

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.serialization.codec.setOf
import net.minecraft.util.ExtraCodecs

/**
 * Determines whether a scope survives a phase transition.
 *
 * @see net.casual.arcade.minigame.scope.MinigameScope
 */
public sealed interface MinigamePhaseLifetime {
    /**
     * Whether a scope with this lifetime survives a transition
     * from [previous] to [next].
     *
     * @param previous The phase being left.
     * @param next The phase being entered.
     * @return Whether the scope survives.
     */
    public fun survives(previous: MinigamePhase, next: MinigamePhase): Boolean

    public fun type(): Type

    /**
     * Survives every transition, only the minigame closing ends it.
     */
    public data object Forever: MinigamePhaseLifetime {
        override fun survives(previous: MinigamePhase, next: MinigamePhase): Boolean {
            return true
        }

        override fun type(): Type {
            return FOREVER
        }
    }

    /**
     * Doesn't survive any phase transition, essentially closes
     * after any phase change.
     */
    public data object Current: MinigamePhaseLifetime {
        override fun survives(previous: MinigamePhase, next: MinigamePhase): Boolean {
            return false
        }

        override fun type(): Type {
            return CURRENT
        }
    }

    /**
     * Survives only if the next phase comes *strictly after* the current one.
     */
    public data object Forward: MinigamePhaseLifetime {
        override fun survives(previous: MinigamePhase, next: MinigamePhase): Boolean {
            return next > previous
        }

        override fun type(): Type {
            return FORWARD
        }
    }

    /**
     * Survives if the next phase is *strictly before* [bound].
     */
    public data class Until(public val bound: MinigamePhase): MinigamePhaseLifetime {
        override fun survives(previous: MinigamePhase, next: MinigamePhase): Boolean {
            return next < this.bound
        }

        override fun type(): Type {
            return UNTIL
        }
    }

    /**
     * Survives if the next phase is in the [phases] set.
     */
    public data class During(public val phases: Set<MinigamePhase>): MinigamePhaseLifetime {
        override fun survives(previous: MinigamePhase, next: MinigamePhase): Boolean {
            return this.phases.contains(next)
        }
        override fun type(): Type {
            return DURING
        }

    }

    /**
     * Survives if the next phase is *strictly between* [lower] and [upper].
     */
    public data class Between(public val lower: MinigamePhase, public val upper: MinigamePhase): MinigamePhaseLifetime {
        override fun survives(previous: MinigamePhase, next: MinigamePhase): Boolean {
            return this.lower < next && next < this.upper
        }

        override fun type(): Type {
            return BETWEEN
        }
    }

    public fun interface Type {
        public fun codec(phase: Codec<MinigamePhase>): MapCodec<out MinigamePhaseLifetime>
    }

    public companion object {
        private val FOREVER = Type { MapCodec.unit(Forever) }
        private val CURRENT = Type { MapCodec.unit(Current) }
        private val FORWARD = Type { MapCodec.unit(Forward) }
        private val UNTIL = Type { phase -> phase.fieldOf("phase").xmap(::Until, Until::bound) }
        private val DURING = Type { phase -> phase.setOf().fieldOf("phases").xmap(::During, During::phases) }
        private val BETWEEN = Type { phase ->
            RecordCodecBuilder.mapCodec<Between> { instance ->
                instance.group(
                    phase.fieldOf("lower").forGetter(Between::lower),
                    phase.fieldOf("upper").forGetter(Between::upper)
                ).apply(instance, ::Between)
            }
        }

        private val TYPES = ExtraCodecs.LateBoundIdMapper<String, Type>()

        init {
            TYPES.put("forever", FOREVER)
            TYPES.put("current", CURRENT)
            TYPES.put("forward", FORWARD)
            TYPES.put("until", UNTIL)
            TYPES.put("during", DURING)
            TYPES.put("between", BETWEEN)
        }

        public fun codec(phase: Codec<MinigamePhase>): Codec<MinigamePhaseLifetime> {
            return TYPES.codec(Codec.STRING).dispatch("type", MinigamePhaseLifetime::type) { type -> type.codec(phase) }
        }
    }
}
