/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.component

import net.minecraft.network.chat.MutableComponent

public fun interface ComponentMutator {
    public fun mutate(component: MutableComponent): MutableComponent

    public companion object {
        public val WRAP: ComponentMutator = ComponentMutator { it.wrap() }
    }
}