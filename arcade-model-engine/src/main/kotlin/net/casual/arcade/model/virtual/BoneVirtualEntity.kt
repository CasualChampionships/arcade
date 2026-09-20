/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.virtual

import com.mojang.math.Transformation
import net.casual.arcade.model.animation.pose.BonePose
import net.casual.arcade.model.geometry.BoneGeometry
import net.casual.arcade.observer.tracker.ObserverTracker
import net.casual.arcade.utils.ItemUtils
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.display.SimpleVirtualItemDisplay
import net.minecraft.world.item.ItemDisplayContext
import org.joml.Matrix4f

public class BoneVirtualEntity internal constructor(
    private val geometry: BoneGeometry,
    attachment: VirtualEntityAttachment,
    observers: ObserverTracker
): SimpleVirtualItemDisplay(attachment, observers) {
    init {
        this.isPassenger = true
        this.setItemDisplayContext(ItemDisplayContext.HEAD)
        this.updateItemStack()
    }

    internal fun pose(pose: BonePose) {
        val matrix = Matrix4f(pose.matrix()).translate(this.geometry.offset).scale(this.geometry.scale)
        this.setTransformation(Transformation(matrix))
    }

    private fun updateItemStack() {
        val stack = ItemUtils.modelled(this.geometry.model)
        this.setItemStack(stack)
    }
}