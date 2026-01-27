/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.nametags.virtual

import net.casual.arcade.nametags.Nametag
import net.casual.arcade.virtual.entity.SimpleParentVirtualEntity
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.display.SimpleVirtualTextDisplay
import net.casual.arcade.virtual.entity.tracker.SimpleObserverTracker
import net.casual.arcade.virtual.entity.utils.attachWithParentObservers
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Entity
import org.joml.Vector3f

public class NametagVirtualEntity(
    attachment: VirtualEntityAttachment,
    private val entity: Entity,
    public val nametag: Nametag
): SimpleParentVirtualEntity(attachment, SimpleObserverTracker()) {
    private val foreground = this.attachWithParentObservers(::SimpleVirtualTextDisplay)
    private val background = this.attachWithParentObservers(::SimpleVirtualTextDisplay)

    private val height = this.attachWithParentObservers { a, o -> NametagHeightVirtualEntity(a, o, a.nametag.height) }

    private var ticks = 0
    private var sneaking = false

    override val canInteractWithChildren: Boolean get() = true

    init {
        this.initializeTextDisplay(this.foreground)
        this.initializeTextDisplay(this.background)

        this.background.setSeeThrough(this.nametag.isVisibleThroughWalls(this.entity))
        this.background.setTextOpacity(30)
        this.foreground.setSeeThrough(false)
        this.foreground.setTextOpacity(127)
        this.foreground.setBackgroundColor(0x00000000)

        val color = this.nametag.backgroundColor
        if (color == null) {
            this.background.setDefaultBackground(true)
        } else {
            this.background.setBackgroundColor(color.color())
        }
    }

    public fun getVehicleId(): Int {
        return this.height.id
    }

    public fun getPassengerIds(): IntArray {
        return intArrayOf(this.foreground.id, this.background.id, this.height.id)
    }

    public fun sneak() {
        // When the player sneaks, the background becomes
        // non-see-through and the foreground becomes invisible
        this.background.setSeeThrough(false)
        this.foreground.setTextOpacity(-128)

        this.sneaking = true
    }

    public fun unsneak() {
        this.background.setSeeThrough(this.nametag.isVisibleThroughWalls(this.entity))
        this.foreground.setTextOpacity(127)

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

    private fun initializeTextDisplay(entity: SimpleVirtualTextDisplay) {
        entity.isPassenger = true
        entity.setBillboardConstraints(Display.BillboardConstraints.CENTER)
        entity.setTranslation(Vector3f(0.0F, -0.2F, 0.0F))
        entity.setInvisible(true)
    }
}