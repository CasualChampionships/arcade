package net.casual.arcade.border

import net.minecraft.util.Mth
import net.minecraft.world.level.border.BorderChangeListener
import org.joml.Vector2d

class MovingCenterBorderState(
    private val border: ArcadeBorder,
    realDuration: Long,
): CenterBorderState {
    private var tickDuration = realDuration / 50.0
    private var ticks = 0

    private var center = Vector2d(0.0,0.0)
    private var targetCenter = Vector2d(0.0,0.0)


    override fun getCenter(): Vector2d {
        val progress = this.ticks/ tickDuration
        val x = if (progress < 1.0) Mth.lerp(progress, this.center.x, targetCenter.x) else targetCenter.x
        val z = if (progress < 1.0) Mth.lerp(progress, center.y, targetCenter.y) else targetCenter.y
        return Vector2d(x, z)
    }

    override fun update(): CenterBorderState {

        if (this.ticks++ % 20 == 0) {
            // We need to update any listeners
            // Most importantly those that send updates to the client
            // This is because the client logic uses real time
            // So if the tick speed has changed we need to tell the client
            for (listener in this.border.listeners) {
                // We do not want to update DelegateBorderChangeListener
                // This updates borders in other dimensions
                if (listener !is BorderChangeListener.DelegateBorderChangeListener) {
                    listener.onBorderCenterSet(this.border, this.center.x, this.center.y)

                }
            }
        }

        return if (this.ticks >= this.tickDuration) StillCenterBorderState(this.center) else this
    }



}