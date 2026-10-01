/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.format

import net.casual.arcade.model.definition.BoneTagResolver
import net.casual.arcade.model.definition.ModelBounds

public data class ModelLoadOptions(
    val addBackfaces: Boolean = false,
    val boundsOverride: ModelBounds? = null,
    val boneTagResolver: BoneTagResolver = BoneTagResolver.Default,
) {
    public companion object {
        public val DEFAULT: ModelLoadOptions = ModelLoadOptions()
    }
}