/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.format.blockbench

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.casual.arcade.model.definition.ModelDefinition
import net.casual.arcade.model.format.ModelFormatException
import net.casual.arcade.model.format.ModelLoader
import net.minecraft.resources.Identifier
import java.io.InputStream

public object BlockbenchModelLoader: ModelLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    public override fun load(id: Identifier, stream: InputStream): ModelDefinition {
        val project = try {
            this.json.decodeFromStream<BlockbenchProject>(stream)
        } catch (e: Exception) {
            throw ModelFormatException("Failed to load blockbench model $id", e)
        }
        return BlockbenchProjectConverter(id, project).convert()
    }
}