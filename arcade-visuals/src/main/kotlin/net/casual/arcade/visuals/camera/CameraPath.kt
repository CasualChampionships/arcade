/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.camera

import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.time.MinecraftTimeDuration

public class CameraPath private constructor(
    public val keyframes: List<Keyframe>,
    public val duration: MinecraftTimeDuration,
    public val interpolator: CameraPathInterpolator
) {
    internal val times: IntArray

    init {
        var currentTick = 0
        this.times = IntArray(this.keyframes.size)
        for ((i, keyframe) in this.keyframes.withIndex()) {
            currentTick += keyframe.duration.ticks
            this.times[i] = currentTick
        }
    }

    public data class Keyframe(
        val location: Location,
        val duration: MinecraftTimeDuration,
    )

    public class Builder {
        private val keyframes = ArrayList<Keyframe>()
        private var start: Location? = null
        private var interpolator: CameraPathInterpolator = CameraPathInterpolator.CatmullRom

        public fun setStart(location: Location): Builder {
            this.start = location
            return this
        }

        public fun addPoint(
            location: Location,
            durationFromPrevious: MinecraftTimeDuration,
        ): Builder {
            this.keyframes.add(Keyframe(location, durationFromPrevious))
            return this
        }

        public fun setInterpolator(interpolator: CameraPathInterpolator): Builder {
            this.interpolator = interpolator
            return this
        }

        public fun build(): CameraPath {
            val start = requireNotNull(this.start) { "Camera path must have a start location" }
            require(this.keyframes.isNotEmpty()) { "Camera path must have at least 1 point after the start" }

            val path = ArrayList<Keyframe>(this.keyframes.size + 1)
            path.add(Keyframe(start, 0.Ticks))
            path.addAll(this.keyframes)

            val totalTicks = path.sumOf { it.duration.ticks }.Ticks
            return CameraPath(path, totalTicks, this.interpolator)
        }
    }
}