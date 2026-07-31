/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.camera

import net.casual.arcade.utils.math.location.Location
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import kotlin.math.exp

public interface CameraPathInterpolator {
    /**
     * Gets the interpolated location for the given path.
     *
     * @param path The full path object (for accessing neighbor times).
     * @param index The index of the "start" keyframe for the current segment.
     * @param tick The absolute number of ticks elapsed since the start of the path.
     * @param progress The 0.0 - 1.0 progress within the current segment.
     */
    public fun interpolate(path: CameraPath, index: Int, tick: Double, progress: Float): Location

    public object Linear: CameraPathInterpolator {
        override fun interpolate(path: CameraPath, index: Int, tick: Double, progress: Float): Location {
            val p1 = path.keyframes[index].location
            val p2 = path.keyframes[index + 1].location
            val pos = p1.position.lerp(p2.position, progress.toDouble())
            val rot = Vec2(
                Mth.rotLerp(progress, p1.rotation.x, p2.rotation.x),
                Mth.rotLerp(progress, p1.rotation.y, p2.rotation.y)
            )
            return Location(pos, rot)
        }
    }

    public object CatmullRom: CameraPathInterpolator {
        override fun interpolate(path: CameraPath, index: Int, tick: Double, progress: Float): Location {
            val frames = path.keyframes
            val p0 = frames[if (index > 0) index - 1 else 0].location
            val p1 = frames[index].location
            val p2 = frames[index + 1].location
            val p3 = frames[if (index < frames.size - 2) index + 2 else frames.size - 1].location

            val pos = this.spline(p0.position, p1.position, p2.position, p3.position, progress)
            val rot = Vec2(
                Mth.rotLerp(progress, p1.rotation.x, p2.rotation.x),
                Mth.rotLerp(progress, p1.rotation.y, p2.rotation.y)
            )
            return Location(pos, rot)
        }

        private fun spline(p0: Vec3, p1: Vec3, p2: Vec3, p3: Vec3, t: Float): Vec3 {
            val tt = t * t
            val ttt = tt * t
            val c0 = p1.scale(2.0)
            val c1 = p2.subtract(p0).scale(t.toDouble())
            val c2 = p0.scale(2.0).subtract(p1.scale(5.0)).add(p2.scale(4.0)).subtract(p3).scale(tt.toDouble())
            val c3 = p0.scale(-1.0).add(p1.scale(3.0)).subtract(p2.scale(3.0)).add(p3).scale(ttt.toDouble())
            return c0.add(c1).add(c2).add(c3).scale(0.5)
        }
    }

    public class Gaussian(private val variance: Double = 0.0): CameraPathInterpolator {
        override fun interpolate(path: CameraPath, index: Int, tick: Double, progress: Float): Location {
            val frames = path.keyframes
            val times = path.times

            var sumX = 0.0; var sumY = 0.0; var sumZ = 0.0
            var sumPitch = 0.0; var sumYaw = 0.0
            var totalWeight = 0.0

            for (i in frames.indices) {
                val pointTime = times[i].toDouble()
                val location = frames[i].location

                val sigma = if (this.variance <= 0.0) {
                    var distSum = 0.0
                    var count = 0
                    if (i + 1 < times.size) {
                        distSum += (times[i + 1] - pointTime)
                        count++
                    }
                    if (i - 1 >= 0) {
                        distSum += (pointTime - times[i - 1])
                        count++
                    }
                    if (count == 0) 1.0 else (0.6 * (distSum / count))
                } else {
                    this.variance
                }

                val weight = this.gaussian(tick, pointTime, sigma)
                if (weight < 1e-6) {
                    continue
                }

                sumX += location.position.x * weight
                sumY += location.position.y * weight
                sumZ += location.position.z * weight

                sumPitch += location.rotation.x * weight
                sumYaw += location.rotation.y * weight

                totalWeight += weight
            }

            if (totalWeight == 0.0) {
                return frames[index].location
            }

            return Location(
                Vec3(sumX / totalWeight, sumY / totalWeight, sumZ / totalWeight),
                Vec2((sumPitch / totalWeight).toFloat(), (sumYaw / totalWeight).toFloat())
            )
        }

        private fun gaussian(x: Double, mu: Double, sigma: Double): Double {
            return exp(-((x - mu) * (x - mu)) / (2 * sigma * sigma))
        }
    }
}