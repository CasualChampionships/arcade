/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.utils

import net.casual.arcade.pack.font.FontResources
import net.casual.arcade.pack.font.spacing.SpacingFontResources
import net.casual.arcade.utils.component.ComponentBuilderContext
import net.casual.arcade.utils.component.ComponentMutator
import net.casual.arcade.utils.component.font
import net.casual.arcade.utils.component.plus
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FontDescription
import net.minecraft.network.chat.MutableComponent

public fun MutableComponent.withDefaultFont(): MutableComponent {
    return this.font(FontDescription.DEFAULT)
}

public fun MutableComponent.withSpacingFont(): MutableComponent {
    return this.font(SpacingFontResources)
}

public fun MutableComponent.withMiniFont(): MutableComponent {
    return this.font(BuiltInFonts.MINI_FONT)
}

public fun MutableComponent.withShiftedDownFont(shift: Int): MutableComponent {
    return this.font(BuiltInFonts.shiftedDownFont(shift))
}

public fun MutableComponent.withMiniShiftedDownFont(shift: Int): MutableComponent {
    return this.font(BuiltInFonts.miniShiftedDownFont(shift))
}

public fun MutableComponent.font(resources: FontResources): MutableComponent {
    return this.font(resources.id)
}

@Suppress("UnusedReceiverParameter")
public fun ComponentBuilderContext.spaced(advance: Float): Component {
    return SpacingFontResources.composed(advance)
}

public inline fun ComponentBuilderContext.offset(
    advance: Float,
    width: Float = 0.0F,
    crossinline block: () -> Component
): ComponentMutator {
    val adjustment = 1.0F - width
    return ComponentMutator { original ->
        original + spaced(advance) + block.invoke() + spaced(-advance + adjustment)
    }
}