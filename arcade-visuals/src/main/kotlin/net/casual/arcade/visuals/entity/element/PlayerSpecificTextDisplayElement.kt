/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.entity.element

import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData
import net.casual.arcade.visuals.elements.PlayerSpecificElement
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityType

public open class PlayerSpecificTextDisplayElement(): PlayerSpecificDisplayElement() {
    public constructor(component: Component): this() {
        this.setText(component)
    }

    public fun setText(component: Component) {
        this.data.modifyEntry(DisplayTrackedData.Text.TEXT) { component }
    }

    public fun setTextFor(observer: ServerPlayer, component: Component) {
        this.data.set(observer.uuid, DisplayTrackedData.Text.TEXT, component)
    }

    public fun setLineWidth(width: Int) {
        this.data.modifyEntry(DisplayTrackedData.Text.LINE_WIDTH) { width }
    }

    public fun setLineWidthFor(observer: ServerPlayer, width: Int) {
        this.data.set(observer.uuid, DisplayTrackedData.Text.LINE_WIDTH, width)
    }

    public fun setTextOpacity(opacity: Byte) {
        this.data.modifyEntry(DisplayTrackedData.Text.TEXT_OPACITY) { opacity }
    }

    public fun setTextOpacityFor(observer: ServerPlayer, opacity: Byte) {
        this.data.set(observer.uuid, DisplayTrackedData.Text.TEXT_OPACITY, opacity)
    }

    public fun setBackgroundColor(color: Int) {
        this.data.modifyEntry(DisplayTrackedData.Text.BACKGROUND) { color }
    }

    public fun setBackgroundColorFor(observer: ServerPlayer, color: Int) {
        this.data.set(observer.uuid, DisplayTrackedData.Text.BACKGROUND, color)
    }

    public fun setTextAlignment(alignment: Display.TextDisplay.Align) {
        this.data.modifyEntry(DisplayTrackedData.Text.TEXT_DISPLAY_FLAGS) { flags ->
            this.updateAlignmentFlags(flags, alignment)
        }
    }

    public fun setTextAlignmentFor(observer: ServerPlayer, alignment: Display.TextDisplay.Align) {
        val flags = this.data.get(observer.uuid, DisplayTrackedData.Text.TEXT_DISPLAY_FLAGS) ?: return
        this.data.set(observer.uuid, DisplayTrackedData.Text.TEXT_DISPLAY_FLAGS, this.updateAlignmentFlags(flags, alignment))
    }

    public fun setShadow(shadow: Boolean) {
        this.modifyDisplayFlagEntry(DisplayTrackedData.Text.SHADOW_FLAG.toInt(), shadow)
    }

    public fun setShadowFor(observer: ServerPlayer, shadow: Boolean) {
        this.modifyDisplayFlagEntryFor(observer, DisplayTrackedData.Text.SHADOW_FLAG.toInt(), shadow)
    }

    public fun setSeeThrough(seeThrough: Boolean) {
        this.modifyDisplayFlagEntry(DisplayTrackedData.Text.SEE_THROUGH_FLAG.toInt(), seeThrough)
    }

    public fun setSeeThroughFor(observer: ServerPlayer, seeThrough: Boolean) {
        this.modifyDisplayFlagEntryFor(observer, DisplayTrackedData.Text.SEE_THROUGH_FLAG.toInt(), seeThrough)
    }

    public fun setDefaultBackground(value: Boolean) {
        this.modifyDisplayFlagEntry(DisplayTrackedData.Text.DEFAULT_BACKGROUND_FLAG.toInt(), value)
    }

    protected fun modifyDisplayFlagEntry(flag: Int, value: Boolean) {
        this.data.modifyEntry(DisplayTrackedData.Text.TEXT_DISPLAY_FLAGS) { flags ->
            flags.updateFlag(flag, value)
        }
    }

    protected fun modifyDisplayFlagEntryFor(observer: ServerPlayer, flag: Int, value: Boolean) {
        val flags = this.data.get(observer.uuid, DisplayTrackedData.Text.TEXT_DISPLAY_FLAGS) ?: return
        this.data.set(observer.uuid, DisplayTrackedData.Text.TEXT_DISPLAY_FLAGS, flags.updateFlag(flag, value))
    }

    protected fun updateAlignmentFlags(flags: Byte, alignment: Display.TextDisplay.Align): Byte {
        return when (alignment) {
            Display.TextDisplay.Align.CENTER -> flags.updateFlag(DisplayTrackedData.Text.LEFT_ALIGNMENT_FLAG.toInt() or DisplayTrackedData.Text.RIGHT_ALIGNMENT_FLAG.toInt(), false)
            Display.TextDisplay.Align.LEFT -> flags.updateFlag(DisplayTrackedData.Text.LEFT_ALIGNMENT_FLAG.toInt(), true).updateFlag(DisplayTrackedData.Text.RIGHT_ALIGNMENT_FLAG.toInt(), false)
            Display.TextDisplay.Align.RIGHT -> flags.updateFlag(DisplayTrackedData.Text.LEFT_ALIGNMENT_FLAG.toInt(), true).updateFlag(DisplayTrackedData.Text.RIGHT_ALIGNMENT_FLAG.toInt(), false)
        }
    }

    override fun getEntityType(): EntityType<*> {
        return EntityType.TEXT_DISPLAY
    }

    private class Dynamic(val element: PlayerSpecificElement<Component>): PlayerSpecificTextDisplayElement() {
        override fun tick() {
            val holder = this.holder
            if (holder != null) {
                for (connection in holder.watchingPlayers) {
                    val observer = connection.player
                    this.setTextFor(observer, this.element.get(observer))
                }
            }

            super.tick()
        }
    }

    public companion object {
        public operator fun invoke(element: PlayerSpecificElement<Component>): PlayerSpecificTextDisplayElement {
            return Dynamic(element)
        }
    }
}