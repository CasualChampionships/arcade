/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.component

import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.contents.PlainTextContents
import net.minecraft.network.chat.contents.TranslatableContents

/**
 * Creates a new [MutableComponent] using the given [builder].
 *
 * @param builder The builder which has context to build components.
 * @return The built component.
 */
@Suppress("FunctionName")
public inline fun <T: Component> Component(builder: ComponentBuilderContext.() -> T): T {
    return ComponentBuilderContext.INSTANCE.builder()
}

/**
 * Appends another component to [this].
 *
 * The equivalent of calling [append].
 *
 * @param other The component to append.
 * @return [this]
 */
public operator fun MutableComponent.plus(other: Component): MutableComponent {
    return this.append(other)
}

/**
 * Appends another component to [this].
 *
 * The equivalent of calling [append].
 *
 * @param other The component to append.
 */
public operator fun MutableComponent.plusAssign(other: Component) {
    this.append(other)
}

/**
 * Appends a literal component of [other] to [this].
 *
 * The equivalent of calling [append].
 *
 * @param other The string to append.
 * @return [this]
 */
public operator fun MutableComponent.plus(other: String): MutableComponent {
    return this.append(other)
}

/**
 * Appends a literal component of [other] to [this].
 *
 * The equivalent of calling [append].
 *
 * @param other The string to append.
 */
public operator fun MutableComponent.plusAssign(other: String) {
    this.append(other)
}

/**
 * Mutates [this] with the given [mutator].
 *
 * @param mutator The component mutator.
 * @return The mutated component.
 */
public operator fun MutableComponent.plus(mutator: ComponentMutator): MutableComponent {
    return mutator.mutate(this)
}

/**
 * Wraps [this] component within an [Component.empty] component.
 *
 * This acts similar to [Component.copy] in that it allows
 * you to convert a [Component] into a [MutableComponent],
 * the difference being this will escape any component
 * styles, as they will be nested inside an inner component.
 *
 * @return The wrapped mutable component.
 */
public fun Component.wrap(): MutableComponent {
    return Component.empty() + this
}

/**
 * Calculates the width of a component, in gui pixels.
 *
 * @return The width of the component.
 */
public fun Component.getWidth(): Int {
    return LiteralWidthResolver.width(this)
}

/**
 * Gets the translation key for the component, given
 * it is translatable, `null` otherwise.
 *
 * @return The translation key, `null` if not translatable.
 */
public fun Component.getTranslationKey(): String? {
    val contents = this.contents as? TranslatableContents ?: return null
    return contents.key
}

/**
 * Checks whether a component is empty.
 *
 * This also checks whether all siblings are empty.
 *
 * @return Whether the component is empty.
 */
public fun Component.isEmpty(): Boolean {
    return this.contents == PlainTextContents.EMPTY && this.siblings.all { it.isEmpty() }
}

/**
 * Appends an [other] component to [this] with the
 * given [space] in between.
 *
 * The space is only added if both [other] and [this]
 * are *not* empty.
 *
 * @return The mutated component.
 */
public fun MutableComponent.appendWithSpace(
    other: Component,
    space: Component = CommonComponents.SPACE
): MutableComponent {
    return when {
        other.isEmpty() -> this
        this.isEmpty() -> this.append(other)
        else -> this.append(space).append(other)
    }
}

/**
 * Joins the components in this iterable into a single
 * [MutableComponent], with the given [separator],
 * optional [prefix] and optional [suffix].
 *
 * @param separator The separator to use between components.
 * @param prefix The prefix to add before the first component.
 * @param suffix The suffix to add after the last component.
 * @return A [MutableComponent] containing all components joined.
 */
public fun Iterable<Component>.join(
    separator: Component? = Component.literal(", "),
    prefix: Component? = null,
    suffix: Component? = null,
): MutableComponent {
    return this.joinToComponent(separator, prefix, suffix) { it }
}

/**
 * Joins the components in this iterable into a single
 * [MutableComponent], with the given [separator],
 * optional [prefix] and optional [suffix],
 * and applies the [transformer] function to each element.
 *
 * @param separator The separator to use between components.
 * @param prefix The prefix to add before the first component.
 * @param suffix The suffix to add after the last component.
 * @param transformer The function to transform each element into a [Component].
 * @return A [MutableComponent] containing all components joined.
 */
public inline fun <T> Iterable<T>.joinToComponent(
    separator: Component? = Component.literal(", "),
    prefix: Component? = null,
    suffix: Component? = null,
    transformer: (T) -> Component
): MutableComponent {
    val component = Component.empty()
    if (prefix != null) {
        component += prefix
    }
    val iterator = this.iterator()
    while (iterator.hasNext()) {
        component += transformer.invoke(iterator.next())
        if (iterator.hasNext() && separator != null) {
            component += separator
        }
    }
    if (suffix != null) {
        component += suffix
    }
    return component
}