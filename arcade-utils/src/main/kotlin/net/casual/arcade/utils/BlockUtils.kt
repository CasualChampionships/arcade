/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

public fun BlockState.isOf(block: Block): Boolean {
    return this.block == block
}

public fun BlockState.isOf(tag: TagKey<Block>): Boolean {
    return this.`is`(tag)
}

public fun BlockState.isOf(key: ResourceKey<Block>): Boolean {
    return this.`is`(key)
}