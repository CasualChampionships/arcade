/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.level

import net.casual.arcade.utils.registries.isOf
import net.minecraft.core.TypedInstance
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

@Deprecated(
    "Use TypedInstance function instead",
    ReplaceWith("this.isOf(block)", "import net.casual.arcade.utils.registries.isOf")
)
public fun BlockState.isOf(block: Block): Boolean {
    return (this as TypedInstance<Block>).isOf(block)
}

@Deprecated(
    "Use TypedInstance function instead",
    ReplaceWith("this.isOf(tag)", "import net.casual.arcade.utils.registries.isOf")
)
public fun BlockState.isOf(tag: TagKey<Block>): Boolean {
    return (this as TypedInstance<Block>).isOf(tag)
}

@Deprecated(
    "Use TypedInstance function instead",
    ReplaceWith("this.isOf(key)", "import net.casual.arcade.utils.registries.isOf")
)
public fun BlockState.isOf(key: ResourceKey<Block>): Boolean {
    return (this as TypedInstance<Block>).isOf(key)
}