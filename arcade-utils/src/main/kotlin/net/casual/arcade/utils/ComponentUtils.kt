/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.casual.arcade.utils.component.font
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.TextColor
import net.minecraft.network.chat.contents.TranslatableContents
import kotlin.reflect.KProperty

@Suppress("DEPRECATION_ERROR")
public object ComponentUtils {
    private val spacing = arcade("spacing")

    private val formattingByColor = Int2ObjectOpenHashMap<ChatFormatting>()

    init {
        for (formatting in ChatFormatting.entries) {
            val color = TextColor.fromLegacyFormat(formatting) ?: continue
            this.formattingByColor[color.value] = formatting
        }
    }

    @Deprecated("For removal")
    public fun negativeWidthOf(component: Component): MutableComponent {
        val key = getTranslationKeyOf(component)
        return Component.translatable("$key.negativeWidth").font(this.spacing)
    }

    @Deprecated("For removal")
    public fun widthDifferenceBetween(first: Component, second: Component): MutableComponent {
        val key = "${getTranslationKeyOf(first)}.difference.${getTranslationKeyOf(second).substringAfterLast('.')}"
        return Component.translatable(key).font(this.spacing)
    }

    public fun getTranslationKeyOf(component: Component): String {
        val contents = component.contents
        if (contents !is TranslatableContents) {
            throw IllegalStateException()
        }
        return contents.key
    }

    @JvmStatic
    public fun colorToFormatting(color: Int): ChatFormatting? {
        return this.formattingByColor[color]
    }

    public fun literal(key: String, modifier: (MutableComponent.() -> Unit)? = null): ConstantComponentGenerator {
        return ConstantComponentGenerator(key, Component::literal, modifier)
    }

    public fun translatable(key: String, modifier: (MutableComponent.() -> Unit)? = null): ConstantComponentGenerator {
        return ConstantComponentGenerator(key, Component::translatable, modifier)
    }

    public fun translatableWithArgs(key: String, modifier: (MutableComponent.() -> Unit)? = null): ComponentGenerator {
        return ComponentGenerator {
            val component = Component.translatable(key, *it)
            modifier?.invoke(component)
            component
        }
    }

    public fun interface ComponentGenerator {
        public fun generate(vararg args: Any): MutableComponent
    }

    public class ConstantComponentGenerator(
        private val key: String,
        private val supplier: (String) -> MutableComponent,
        private val consumer: (MutableComponent.() -> Unit)?,
    ) {
        public fun generate(): MutableComponent {
            val component = this.supplier(this.key)
            this.consumer?.invoke(component)
            return component
        }

        public fun with(mutator: MutableComponent.() -> Unit): ConstantComponentGenerator {
            val consumer = this.consumer
            return if (consumer != null) {
                ConstantComponentGenerator(this.key, this.supplier) {
                    consumer()
                    mutator()
                }
            } else {
                ConstantComponentGenerator(this.key, this.supplier, mutator)
            }
        }

        public operator fun getValue(any: Any, property: KProperty<*>): MutableComponent {
            return this.generate()
        }
    }
}