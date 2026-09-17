/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.format.blockbench

import kotlinx.serialization.Serializable

@Serializable
internal class BlockbenchProjectV5(
    override val meta: Metadata,
    override val name: String?,
    override val resolution: Resolution?,
    override val elements: List<Element>,
    override val textures: List<Texture>,
    override val animations: List<Animation>
): BlockbenchProject() {
    override val flipAnimationAxes: Boolean
        get() = false

    override fun outliner(): List<Any> {
        TODO("Not yet implemented")
    }
}