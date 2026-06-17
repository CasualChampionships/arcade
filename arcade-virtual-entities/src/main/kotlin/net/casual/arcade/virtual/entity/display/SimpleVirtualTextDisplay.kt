/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.display

import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.tracker.ObserverTracker
import net.casual.arcade.virtual.entity.tracker.SimpleObserverTracker
import net.casual.arcade.virtual.entity.utils.EntityDataSharedFlags
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityTypes
import kotlin.experimental.or
import net.casual.arcade.virtual.entity.utils.EntityDataAccessors.Display.Text as TextDisplayDataAccessors
import net.casual.arcade.virtual.entity.utils.EntityDataSharedFlags.Display.Text as TextDisplayDataSharedFlags

public open class SimpleVirtualTextDisplay(
    attachment: VirtualEntityAttachment,
    observers: ObserverTracker = SimpleObserverTracker()
): SimpleVirtualDisplay(EntityTypes.TEXT_DISPLAY, attachment, observers) {
    public fun setText(component: Component) {
        this.setDataEntry(TextDisplayDataAccessors.TEXT, component)
    }

    public fun setTextFor(observer: ServerPlayer, component: Component) {
        this.setDataEntryFor(observer, TextDisplayDataAccessors.TEXT, component)
    }

    public fun setTextToBaseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, TextDisplayDataAccessors.TEXT)
    }

    public fun modifyText(modifier: (Component) -> Component) {
        this.modifyDataEntry(TextDisplayDataAccessors.TEXT, modifier)
    }

    public fun setLineWidth(width: Int) {
        this.setDataEntry(TextDisplayDataAccessors.LINE_WIDTH, width)
    }

    public fun setLineWidthFor(observer: ServerPlayer, width: Int) {
        this.setDataEntryFor(observer, TextDisplayDataAccessors.LINE_WIDTH, width)
    }

    public fun setTextOpacity(opacity: Byte) {
        this.setDataEntry(TextDisplayDataAccessors.TEXT_OPACITY, opacity)
    }

    public fun setTextOpacityFor(observer: ServerPlayer, opacity: Byte) {
        this.setDataEntryFor(observer, TextDisplayDataAccessors.TEXT_OPACITY, opacity)
    }

    public fun setBackgroundColor(color: Int) {
        this.setDataEntry(TextDisplayDataAccessors.BACKGROUND_COLOR, color)
    }

    public fun setBackgroundColorFor(observer: ServerPlayer, color: Int) {
        this.setDataEntryFor(observer, TextDisplayDataAccessors.BACKGROUND_COLOR, color)
    }

    public fun setTextAlignment(alignment: Display.TextDisplay.Align) {
        this.modifyDataEntry(TextDisplayDataAccessors.STYLE_FLAGS) { flags ->
            this.updateAlignmentFlags(flags, alignment)
        }
    }

    public fun setTextAlignmentFor(observer: ServerPlayer, alignment: Display.TextDisplay.Align) {
        val flags = this.data.get(observer.uuid, TextDisplayDataAccessors.STYLE_FLAGS) ?: return
        this.setDataEntryFor(observer, TextDisplayDataAccessors.STYLE_FLAGS, this.updateAlignmentFlags(flags, alignment))
    }

    public fun setShadow(shadow: Boolean) {
        this.modifyDisplayFlagEntry(TextDisplayDataSharedFlags.SHADOW, shadow)
    }

    public fun setShadowFor(observer: ServerPlayer, shadow: Boolean) {
        this.modifyDisplayFlagEntryFor(observer, TextDisplayDataSharedFlags.SHADOW, shadow)
    }

    public fun setSeeThrough(seeThrough: Boolean) {
        this.modifyDisplayFlagEntry(TextDisplayDataSharedFlags.SEE_THROUGH, seeThrough)
    }

    public fun setSeeThroughFor(observer: ServerPlayer, seeThrough: Boolean) {
        this.modifyDisplayFlagEntryFor(observer, TextDisplayDataSharedFlags.SEE_THROUGH, seeThrough)
    }

    public fun setDefaultBackground(value: Boolean) {
        this.modifyDisplayFlagEntry(TextDisplayDataSharedFlags.USE_DEFAULT_BACKGROUND, value)
    }

    protected fun modifyDisplayFlagEntry(flag: Byte, value: Boolean) {
        this.modifyDataEntry(TextDisplayDataAccessors.STYLE_FLAGS) { flags ->
            flags.updateFlag(flag, value)
        }
    }

    protected fun modifyDisplayFlagEntryFor(observer: ServerPlayer, flag: Byte, value: Boolean) {
        val flags = this.data.get(observer.uuid, TextDisplayDataAccessors.STYLE_FLAGS) ?: return
        this.setDataEntryFor(observer, TextDisplayDataAccessors.STYLE_FLAGS, flags.updateFlag(flag, value))
    }

    protected fun updateAlignmentFlags(flags: Byte, alignment: Display.TextDisplay.Align): Byte {
        return when (alignment) {
            Display.TextDisplay.Align.CENTER -> flags.updateFlag(TextDisplayDataSharedFlags.ALIGN_LEFT or TextDisplayDataSharedFlags.ALIGN_RIGHT, false)
            Display.TextDisplay.Align.LEFT -> flags.updateFlag(TextDisplayDataSharedFlags.ALIGN_LEFT, true).updateFlag(TextDisplayDataSharedFlags.ALIGN_RIGHT, false)
            Display.TextDisplay.Align.RIGHT -> flags.updateFlag(TextDisplayDataSharedFlags.ALIGN_LEFT, true).updateFlag(TextDisplayDataSharedFlags.ALIGN_RIGHT, false)
        }
    }

    private fun Byte.updateFlag(flag: Byte, value: Boolean): Byte {
        return EntityDataSharedFlags.updateFlag(this, flag, value)
    }
}