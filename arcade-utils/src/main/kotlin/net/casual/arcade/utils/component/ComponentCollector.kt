/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.component

import net.minecraft.network.chat.FormattedText

// Taken from `net.minecraft.client.ComponentCollector`
internal class ComponentCollector {
    private val parts = ArrayList<FormattedText>()

    fun append(part: FormattedText) {
        this.parts.add(part)
    }

    fun result(): FormattedText? {
        return when {
            this.parts.isEmpty() -> null
            this.parts.size == 1 -> this.parts[0]
            else -> FormattedText.composite(this.parts)
        }
    }

    fun resultOrEmpty(): FormattedText {
        return this.result() ?: FormattedText.EMPTY
    }

    fun reset() {
        this.parts.clear()
    }
}