/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.component

import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSink
import net.minecraft.util.StringDecomposer
import java.util.*
import kotlin.math.max

public class ComponentSplitter(
    private val resolver: WidthResolver
) {
    public fun splitLines(
        content: FormattedText,
        maxWidth: Int,
        style: Style
    ): List<FormattedText> {
        val lines = ArrayList<FormattedText>()
        this.splitLines(content, maxWidth, style) { text, _ -> lines.add(text) }
        return lines
    }

    public fun splitLines(
        content: FormattedText,
        maxWidth: Int,
        style: Style,
        splitifier: (FormattedText, Boolean) -> Unit
    ) {
        val lines = ArrayList<LineComponent>()
        content.visit({ contentStyle, contentString ->
            if (contentString.isNotEmpty()) {
                lines.add(LineComponent(contentString, contentStyle))
            }
            Optional.empty()
        }, style)

        val flat = FlatComponents(lines)
        var shouldContinue = true
        var hadAnyNewlines = false
        var isOnSameLine = false

        while (shouldContinue) {
            shouldContinue = false
            val finder = LineBreakFinder.of(maxWidth, this.resolver)
            for (component in flat.parts) {
                val hasSplit = !StringDecomposer.iterateFormatted(component.contents, 0, component.style, style, finder)
                if (hasSplit) {
                    val pos = finder.getSplitPosition()
                    val splitStyle = finder.getSplitStyle()
                    val char = flat.get(pos)
                    val isNewline = char == '\n'
                    val isNewlineOrSpace = isNewline || char == ' '
                    hadAnyNewlines = isNewline
                    val text = flat.split(pos, if (isNewlineOrSpace) 1 else 0, splitStyle)
                    splitifier.invoke(text, isOnSameLine)
                    isOnSameLine = !isNewline
                    shouldContinue = true
                    break
                }

                finder.addToOffset(component.contents.length)
            }
        }

        val remainder = flat.remainder()
        if (remainder != null) {
            splitifier.invoke(remainder, isOnSameLine)
        } else if (hadAnyNewlines) {
            splitifier.invoke(FormattedText.EMPTY, false)
        }
    }

    private class FlatComponents(
        val parts: MutableList<LineComponent>
    ) {
        var flattened = this.parts.joinToString(separator = "", transform = LineComponent::contents)

        fun get(index: Int): Char {
            return this.flattened[index]
        }

        fun split(begin: Int, end: Int, style: Style): FormattedText {
            val collector = ComponentCollector()
            val iterator = this.parts.listIterator()

            var i = begin
            var reachedEnd = false
            for (component in iterator) {
                val content = component.contents
                val length = content.length
                if (!reachedEnd) {
                    if (i <= length) {
                        val substring = content.substring(0, i)
                        if (substring.isNotEmpty()) {
                            collector.append(FormattedText.of(substring, component.style))
                        }
                        i += end
                        reachedEnd = true
                    } else {
                        collector.append(component)
                        iterator.remove()
                        i -= length
                    }
                }

                if (reachedEnd) {
                    if (i <= length) {
                        val substring = content.substring(i)
                        if (substring.isEmpty()) {
                            iterator.remove()
                        } else {
                            iterator.set(LineComponent(substring, style))
                        }
                        break
                    }

                    iterator.remove()
                    i -= length
                }
            }

            this.flattened = this.flattened.substring(begin + end)
            return collector.resultOrEmpty()
        }

        fun remainder(): FormattedText? {
            val collector = ComponentCollector()
            this.parts.forEach(collector::append)
            this.parts.clear()
            return collector.result()
        }
    }

    private class LineBreakFinder private constructor(
        private val maxWidth: Int,
        private val resolver: WidthResolver,
        private val prioritizeSpaces: Boolean
    ): FormattedCharSink {
        private var lineBreak = -1
        private var lineBreakStyle = Style.EMPTY
        private var hadNonZeroWidthChar = false
        private var width = 0.0F
        private var lastSpace = -1
        private var lastSpaceStyle = Style.EMPTY
        private var nextChar = 0
        private var offset = 0

        override fun accept(i: Int, style: Style, j: Int): Boolean {
            val k = i + this.offset

            if (j == 10) {
                return this.finishIteration(k, style)
            }

            if (j == 32) {
                this.lastSpace = k
                this.lastSpaceStyle = style
            }

            val f = this.resolver.width(j, style)
            this.width += f
            this.nextChar = k + Character.charCount(j)

            if (!this.hadNonZeroWidthChar || this.width <= this.maxWidth) {
                this.hadNonZeroWidthChar = this.hadNonZeroWidthChar or (f != 0)
                return true
            }

            if (this.prioritizeSpaces && this.lastSpace != -1) {
                return this.finishIteration(this.lastSpace, this.lastSpaceStyle)
            }
            // return this.finishIteration(k, style)
            return this.finishIteration(this.nextChar, style)
        }

        fun getSplitPosition(): Int {
            return if (this.lineBreak != -1) this.lineBreak else this.nextChar
        }

        fun getSplitStyle(): Style {
            return this.lineBreakStyle
        }

        fun addToOffset(offset: Int) {
            this.offset += offset
        }

        private fun finishIteration(lineBreak: Int, lineBreakStyle: Style): Boolean {
            this.lineBreak = lineBreak
            this.lineBreakStyle = lineBreakStyle
            return false
        }

        companion object {
            fun of(maxWidth: Int, provider: WidthResolver, prioritizeSpaces: Boolean = true): LineBreakFinder {
                return LineBreakFinder(max(maxWidth, 1), provider, prioritizeSpaces)
            }
        }
    }

    private class LineComponent(
        val contents: String,
        val style: Style
    ): FormattedText {
        override fun <T: Any> visit(acceptor: FormattedText.ContentConsumer<T>): Optional<T> {
            return acceptor.accept(this.contents)
        }

        override fun <T: Any> visit(acceptor: FormattedText.StyledContentConsumer<T>, style: Style): Optional<T> {
            return acceptor.accept(this.style, this.contents)
        }
    }

    public companion object {
        public val LITERAL: ComponentSplitter = ComponentSplitter(LiteralWidthResolver)
    }
}