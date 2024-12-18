/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.font.spacing

import com.google.common.cache.CacheBuilder
import net.casual.arcade.resources.font.FontPUA
import net.casual.arcade.resources.font.IndexedFontResources
import net.casual.arcade.utils.ResourceUtils
import net.minecraft.network.chat.Component
import java.util.*
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sign
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

public object SpacingFontResources: IndexedFontResources(ResourceUtils.arcade("spacing"), FontPUA.Plane15) {
    private const val SIZE = 4096
    private val INT_SIZE_RANGE = -SIZE..SIZE

    private val floats = TreeMap<Float, Component>()

    private val cache = CacheBuilder.newBuilder()
        .expireAfterAccess(1.minutes.toJavaDuration())
        .build<Float, Component>()

    init {
        for (size in INT_SIZE_RANGE) {
            this.indexed { this.space(size.toFloat()) }
        }

        for (i in 1..100) {
            val denominator = i + 1.0F
            for (j in -i..i) {
                val advance = j / denominator
                this.floats[advance] = this.space(advance)
            }
        }
    }

    public fun spaced(advance: Int): Component {
        require(advance in INT_SIZE_RANGE) {
            "Advance is outside of range of spaced(), consider using compose()"
        }
        return this.get(advance + SIZE)
    }

    public fun spaced(advance: Float): Component {
        if (floor(advance) == advance) {
            return spaced(advance.toInt())
        }
        return requireNotNull(this.floats[advance]) {
            "Advance is outside of range of spaced(), consider using compose()"
        }
    }

    public fun composed(advance: Int): Component {
        if (advance in INT_SIZE_RANGE) {
            return spaced(advance)
        }
        val advanceAsFloat = advance.toFloat()
        val initial = this.cache.getIfPresent(advanceAsFloat)
        if (initial != null) {
            return initial
        }

        val composed = Component.empty()
        var remaining = abs(advance)
        val max = this.get(((advance.sign + 1) / 2) * SIZE * 2)

        do {
            composed.append(max)
            remaining -= SIZE
        } while (remaining > SIZE)
        composed.append(this.spaced(remaining * advance.sign))
        this.cache.put(advanceAsFloat, composed)
        return composed
    }

    public fun composed(advance: Float, error: Float = 0.01F): Component {
        val initial = this.cache.getIfPresent(advance) ?: this.floats[advance]
        if (initial != null) {
            return initial
        }

        val sign = sign(advance).toInt()
        val absAdvance = abs(advance)
        val integral = absAdvance.toInt()
        var remaining = absAdvance - integral
        if (remaining == 0.0F) {
            return this.composed(integral * sign)
        }

        val component = Component.empty()
        component.append(this.composed(integral * sign))
        while (remaining > error) {
            val exact = this.floats[remaining * sign]
            if (exact != null) {
                component.append(exact)
                break
            }
            val closest = if (sign > 0) this.floats.floorEntry(remaining) else this.floats.ceilingEntry(-remaining)
            closest ?: break
            component.append(closest.value)
            remaining -= closest.key * sign
        }
        this.cache.put(advance, component)
        return component
    }
}