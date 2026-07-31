/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.nametags.virtual

import net.casual.arcade.nametags.Nametag
import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.tracker.SimpleObserverTracker
import net.casual.arcade.observer.utils.asPlayerOrNull
import net.casual.arcade.virtual.entity.SimpleParentVirtualEntity
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.display.SimpleVirtualTextDisplay
import net.casual.arcade.virtual.entity.location.VirtualPosition
import net.casual.arcade.virtual.entity.location.VirtualRotation
import net.casual.arcade.virtual.entity.utils.attachWithParentObservers
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

public class NametagVirtualEntity(
    attachment: VirtualEntityAttachment,
    private val entity: Entity,
    public val nametag: Nametag
): SimpleParentVirtualEntity(attachment) {
    private val foreground = this.attachWithParentObservers(::SimpleVirtualTextDisplay)
    private val background = this.attachWithParentObservers(::SimpleVirtualTextDisplay)

    private val height = this.attachWithParentObservers { a, o -> NametagHeightVirtualEntity(a, o, a.nametag.height) }

    private var ticks = 0
    private var sneaking = false

    init {
        this.initializeTextDisplay(this.foreground)
        this.initializeTextDisplay(this.background)

        this.background.setSeeThrough(this.nametag.isVisibleThroughWalls(this.entity))
        this.background.setTextOpacity(30)
        this.foreground.setSeeThrough(false)
        this.foreground.setTextOpacity(-1)
        // This is a weird hack, if we set the background color
        // to 0 then Minecraft renders it in a different order
        this.foreground.setBackgroundColor(1)

        val color = this.nametag.backgroundColor
        if (color == null) {
            this.background.setDefaultBackground(true)
        } else {
            this.background.setBackgroundColor(color.color())
        }
    }

    public fun getVehicle(): NametagHeightVirtualEntity {
        return this.height
    }

    public fun getPassengerIds(): IntArray {
        return intArrayOf(this.foreground.id, this.background.id, this.height.id)
    }

    public fun sneak() {
        // When the player sneaks, the background becomes
        // non-see-through and the foreground becomes invisible
        this.background.setSeeThrough(false)
        this.foreground.setTextOpacity(-127)

        this.sneaking = true
    }

    public fun unsneak() {
        this.background.setSeeThrough(this.nametag.isVisibleThroughWalls(this.entity))
        this.foreground.setTextOpacity(-1)

        this.sneaking = false
    }

    override fun updateChildren() {
        val interval = this.nametag.updateInterval.ticks
        if (interval > 0 && this.ticks++ % interval == 0) {
            this.updateNametag()
        }
    }

    public fun updateNametag() {
        val updated = this.nametag.getComponent(this.entity)
        this.foreground.setText(updated)
        this.background.setText(updated)

        this.background.setSeeThrough(!this.sneaking && this.nametag.isVisibleThroughWalls(this.entity))
    }

    override fun canObserve(observer: Observer): Boolean {
        val player = observer.asPlayerOrNull()
        return (player == null || this.entity.broadcastToPlayer(player))
            && this.nametag.isObservable(this.entity, observer)
            && this.nametag.isWithinRange(this.entity, observer)
    }

    private fun initializeTextDisplay(entity: SimpleVirtualTextDisplay) {
        entity.isPassenger = true
        // We shift the text super far down in the world so the text isn't visibly lerping
        entity.position = VirtualPosition.Relative(Vec3(0.0, -1000.0, 0.0))
        entity.rotation = VirtualRotation.Absolute(Vec2.ZERO)
        entity.setBillboardConstraints(Display.BillboardConstraints.CENTER)
        entity.setTranslation(Vector3f(0.0F, -0.2F, 0.0F))
        entity.setInvisible(true)
    }
}