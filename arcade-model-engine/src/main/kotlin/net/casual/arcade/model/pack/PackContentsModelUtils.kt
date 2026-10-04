/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.pack

import net.casual.arcade.model.definition.ModelDefinition
import net.casual.arcade.pack.generation.PackContents

public fun PackContents.addModel(resources: ModelResources) {
    for ((id, image, meta) in resources.getTextures()) {
        this.addFile(PackContents.textureFilePath(id), image)
        if (meta != null) {
            this.addFile(PackContents.texturesMetadataFilePath(id), meta)
        }
    }
    for ((id, json) in resources.getModelJsons()) {
        this.addFile(PackContents.modelsFilePath(id), json)
    }
    for ((id, json) in resources.getItemJsons()) {
        this.addFile(PackContents.itemFilePath(id), json)
    }
}

public fun PackContents.addModel(definition: ModelDefinition) {
    this.addModel(ModelResources(definition))
}