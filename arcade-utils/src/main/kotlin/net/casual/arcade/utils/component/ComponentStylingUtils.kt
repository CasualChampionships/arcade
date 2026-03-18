/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.component

import it.unimi.dsi.fastutil.ints.IntList
import net.casual.arcade.utils.ColorUtils
import net.casual.arcade.utils.Identifier
import net.casual.arcade.utils.color.ColorARGB
import net.casual.arcade.utils.color.ColorOklab
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.*
import net.minecraft.network.chat.HoverEvent.EntityTooltipInfo
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStackTemplate
import java.net.URI

/**
 * Sets the color of the component to the given [color].
 *
 * @param color The color to set.
 * @return [this]
 */
public fun MutableComponent.color(color: Int): MutableComponent {
    return this.withStyle { style -> style.withColor(color) }
}

/**
 * Sets the shadow color of the component to the given [color].
 *
 * @param color The color to set.
 * @return [this]
 */
public fun MutableComponent.shadow(color: Int): MutableComponent {
    return this.withStyle { style -> style.withShadowColor(color) }
}

/**
 * Removes the shadow from the component.
 *
 * This sets the shadow color to transparent (0x00000000).
 *
 * @return [this]
 */
public fun MutableComponent.shadowless(): MutableComponent {
    return this.shadow(0x00000000)
}

/**
 * Sets the color of the component to black.
 *
 * @return [this]
 */
public fun MutableComponent.black(): MutableComponent {
    return this.withStyle(ChatFormatting.BLACK)
}

/**
 * Sets the color of the component to dark blue.
 *
 * @return [this]
 */
public fun MutableComponent.navy(): MutableComponent {
    return this.withStyle(ChatFormatting.DARK_BLUE)
}

/**
 * Sets the color of the component to dark green.
 *
 * @return [this]
 */
public fun MutableComponent.green(): MutableComponent {
    return this.withStyle(ChatFormatting.DARK_GREEN)
}

/**
 * Sets the color of the component to dark aqua.
 *
 * @return [this]
 */
public fun MutableComponent.teal(): MutableComponent {
    return this.withStyle(ChatFormatting.DARK_AQUA)
}

/**
 * Sets the color of the component to dark red.
 *
 * @return [this]
 */
public fun MutableComponent.crimson(): MutableComponent {
    return this.withStyle(ChatFormatting.DARK_RED)
}

/**
 * Sets the color of the component to dark purple.
 *
 * @return [this]
 */
public fun MutableComponent.purple(): MutableComponent {
    return this.withStyle(ChatFormatting.DARK_PURPLE)
}

/**
 * Sets the color of the component to gold.
 *
 * @return [this]
 */
public fun MutableComponent.gold(): MutableComponent {
    return this.withStyle(ChatFormatting.GOLD)
}

/**
 * Sets the color of the component to gray.
 *
 * @return [this]
 */
public fun MutableComponent.silver(): MutableComponent {
    return this.withStyle(ChatFormatting.GRAY)
}

/**
 * Sets the color of the component to dark gray.
 *
 * @return [this]
 */
public fun MutableComponent.gray(): MutableComponent {
    return this.withStyle(ChatFormatting.DARK_GRAY)
}

/**
 * Sets the color of the component to blue.
 *
 * @return [this]
 */
public fun MutableComponent.blue(): MutableComponent {
    return this.withStyle(ChatFormatting.BLUE)
}

/**
 * Sets the color of the component to green.
 *
 * @return [this]
 */
public fun MutableComponent.lime(): MutableComponent {
    return this.withStyle(ChatFormatting.GREEN)
}

/**
 * Sets the color of the component to aqua.
 *
 * @return [this]
 */
public fun MutableComponent.aqua(): MutableComponent {
    return this.withStyle(ChatFormatting.AQUA)
}

/**
 * Sets the color of the component to red.
 *
 * @return [this]
 */
public fun MutableComponent.red(): MutableComponent {
    return this.withStyle(ChatFormatting.RED)
}

/**
 * Sets the color of the component to light purple.
 *
 * @return [this]
 */
public fun MutableComponent.pink(): MutableComponent {
    return this.withStyle(ChatFormatting.LIGHT_PURPLE)
}

/**
 * Sets the color of the component to yellow.
 *
 * @return [this]
 */
public fun MutableComponent.yellow(): MutableComponent {
    return this.withStyle(ChatFormatting.YELLOW)
}

/**
 * Sets the color of the component to white.
 *
 * @return [this]
 */
public fun MutableComponent.white(): MutableComponent {
    return this.withStyle(ChatFormatting.WHITE)
}

/**
 * Bolds the component.
 *
 * @return [this]
 */
public fun MutableComponent.bold(): MutableComponent {
    return this.withStyle { style -> style.withBold(true) }
}

/**
 * Unbolds the component.
 *
 * @return [this]
 */
public fun MutableComponent.unbold(): MutableComponent {
    return this.withStyle { style -> style.withBold(false) }
}

/**
 * Italicizes the component.
 *
 * @return [this]
 */
public fun MutableComponent.italicize(): MutableComponent {
    return this.withStyle { style -> style.withItalic(true) }
}

/**
 * Unitalicizes the component.
 *
 * @return [this]
 */
public fun MutableComponent.unitalicize(): MutableComponent {
    return this.withStyle { style -> style.withItalic(false) }
}

/**
 * Underlines the component.
 *
 * @return [this]
 */
public fun MutableComponent.underline(): MutableComponent {
    return this.withStyle { style -> style.withUnderlined(true) }
}

/**
 * Adds a strikethrough to the component.
 *
 * @return [this]
 */
public fun MutableComponent.strikethrough(): MutableComponent {
    return this.withStyle { style -> style.withStrikethrough(true) }
}

/**
 * Adds a click event to the component.
 *
 * @param event The click event to trigger.
 * @return [this]
 */
public fun MutableComponent.click(event: ClickEvent): MutableComponent {
    return this.withStyle { style -> style.withClickEvent(event) }
}

/**
 * Adds a command click event to the component.
 *
 * This will run the given [command] when clicked.
 *
 * @param command The command to run.
 * @return [this]
 */
public fun MutableComponent.command(command: String): MutableComponent {
    return this.click(ClickEvent.RunCommand(command))
}

/**
 * Adds a suggest command click event to the component.
 *
 * This will suggest the given [command] when clicked.
 *
 * @param command The command to suggest.
 * @return [this]
 */
public fun MutableComponent.suggestCommand(command: String): MutableComponent {
    return this.click(ClickEvent.SuggestCommand(command))
}

/**
 * Adds a link click event to the component.
 *
 * This will open the given [link] when clicked.
 *
 * @param link The URL to open.
 * @return [this]
 */
public fun MutableComponent.link(link: URI): MutableComponent {
    return this.click(ClickEvent.OpenUrl(link))
}

/**
 * Adds a link click event to the component.
 *
 * This will open the given [link] when clicked.
 *
 * @param link The URL to open.
 * @return [this]
 */
public fun MutableComponent.link(link: String): MutableComponent {
    return this.link(URI.create(link))
}

/**
 * Adds a hover event to the component.
 *
 * @param event The hover event to trigger.
 * @return [this]
 */
public fun MutableComponent.hover(event: HoverEvent): MutableComponent {
    return this.withStyle { style -> style.withHoverEvent(event) }
}

/**
 * Adds a hover event to the component.
 *
 * This will show the given [component] when hovered.
 *
 * @param component The text to show on hover.
 * @return [this]
 */
public fun MutableComponent.hover(component: Component): MutableComponent {
    return this.hover(HoverEvent.ShowText(component))
}

/**
 * Adds a hover event to the component.
 *
 * This will show the given [entity] when hovered.
 *
 * @param entity The Entity info to show on hover.
 * @return [this]
 */
public fun MutableComponent.hover(entity: Entity): MutableComponent {
    return this.hover(HoverEvent.ShowEntity(EntityTooltipInfo(entity.type, entity.uuid, entity.name)))
}

/**
 * Adds a hover event to the component.
 *
 * This will show the given [stack] when hovered.
 *
 * @param stack The item stack to show on hover.
 * @return [this]
 */
public fun MutableComponent.hover(stack: ItemStackTemplate): MutableComponent {
    return this.hover(HoverEvent.ShowItem(stack))
}

/**
 * Sets the font of the component to the given [font].
 *
 * @param font The font description.
 * @return [this]
 */
public fun MutableComponent.font(font: FontDescription): MutableComponent {
    return this.withStyle { style -> style.withFont(font) }
}

/**
 * Sets the font of the component to the given [font].
 *
 * @param font The resource location of the font to use.
 * @return [this]
 */
public fun MutableComponent.font(font: Identifier): MutableComponent {
    return this.font(FontDescription.Resource(font))
}

/**
 * Sets the font of the component to the given [namespace] and [path].
 *
 * @param namespace The namespace of the font.
 * @param path The path of the font.
 * @return [this]
 */
public fun MutableComponent.font(namespace: String, path: String): MutableComponent {
    return this.font(Identifier(namespace, path))
}

/**
 * Maps all the colors in the component such
 * that the component becomes grayscale.
 *
 * @return The component in grayscale.
 * @see mapColors
 */
public fun Component.grayscale(): MutableComponent {
    return this.mapColors(fun(color): TextColor? {
        if (color !== null) {
            return TextColor.fromRgb(ColorUtils.greyscale(color.value))
        }
        return null
    })
}

/**
 * Maps all colors in the component recursively.
 *
 * @param mapper The color mapper.
 * @return The color mapped component.
 */
public fun Component.mapColors(mapper: (TextColor?) -> TextColor?): MutableComponent {
    val copy = this.plainCopy()
    copy.style = this.style.withColor(mapper.invoke(this.style.color))
    for (siblings in this.siblings) {
        copy.append(siblings.mapColors(mapper))
    }
    return copy
}

/**
 * Creates a literal component with a given gradient.
 *
 * @param text The literal text.
 * @param color1 The first color in the gradient.
 * @param color2 The second color in the gradient.
 * @param colors The remaining colors in the gradient.
 * @return The [MutableComponent].
 */
public fun ComponentBuilderContext.literal(
    text: String,
    color1: Int,
    color2: Int,
    vararg colors: Int
): MutableComponent {
    return this.literal(text, IntList.of(color1, color2, *colors))
}

/**
 * Creates a literal component with a given gradient.
 *
 * @param text The literal text.
 * @param gradient The colors that form the gradient.
 * @return The [MutableComponent].
 */
@Suppress("UnusedReceiverParameter")
public fun ComponentBuilderContext.literal(text: String, gradient: IntList): MutableComponent {
    require(gradient.size >= 2)

    val component = Component.empty()
    val colors = gradient.intStream().mapToObj(ColorOklab::from).toList()

    val segments = colors.size - 1
    for (i in text.indices) {
        val t = i.toDouble() / (text.length - 1).coerceAtLeast(1)
        val segment = (t * segments).toInt().coerceAtMost(segments - 1)
        val lt = (t * segments) - segment

        val c1 = colors[segment]
        val c2 = colors[segment + 1]

        val interpolated = ColorOklab(
            l = c1.l + (c2.l - c1.l) * lt,
            a = c1.a + (c2.a - c1.a) * lt,
            b = c1.b + (c2.b - c1.b) * lt
        )

        val argb = ColorARGB.from(interpolated)
        component += Component.literal(text[i].toString()).color(argb.color())
    }

    return component
}