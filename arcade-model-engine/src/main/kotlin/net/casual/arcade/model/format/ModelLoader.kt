/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.format

import net.casual.arcade.model.definition.ModelDefinition
import net.minecraft.resources.Identifier
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.inputStream

public interface ModelLoader {
    public fun load(id: Identifier, stream: InputStream): ModelDefinition

    public fun load(id: Identifier, path: Path): ModelDefinition {
        return path.inputStream().use { stream -> this.load(id, stream) }
    }
}