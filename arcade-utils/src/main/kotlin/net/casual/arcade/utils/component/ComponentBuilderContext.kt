/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.component

import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

public class ComponentBuilderContext {
    public val nl: Component
        get() = this.newline()

    public fun empty(): MutableComponent {
        return Component.empty()
    }

    public fun literal(text: String): MutableComponent {
        return Component.literal(text)
    }

    public fun translatable(key: String): MutableComponent {
        return Component.translatable(key)
    }

    public fun translatable(key: String, vararg args: Any): MutableComponent {
        return Component.translatable(key, args)
    }

    public fun newline(): Component {
        return CommonComponents.NEW_LINE
    }

    public fun wrap(): ComponentMutator {
        return ComponentMutator.WRAP
    }

    public companion object {
        public val INSTANCE: ComponentBuilderContext = ComponentBuilderContext()
    }
}