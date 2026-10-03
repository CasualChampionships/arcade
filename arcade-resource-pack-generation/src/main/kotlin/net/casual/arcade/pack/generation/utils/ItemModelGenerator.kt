/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.generation.utils

import net.casual.arcade.pack.generation.PackContents
import net.casual.arcade.utils.Identifier
import net.minecraft.resources.Identifier
import java.nio.file.FileVisitResult
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.notExists
import kotlin.io.path.visitFileTree

internal object ItemModelGenerator {
    fun generateMissing(contents: PackContents, namespace: String, root: Path) {
        val textures = root.resolve(this.itemTexturesPath(namespace))
        val models = root.resolve(this.itemModelsPath(namespace))
        val items = root.resolve(PackContents.itemsPath(namespace))

        if (textures.isDirectory()) {
            textures.visitFileTree {
                onVisitFile { path, _ ->
                    val relative = relativize(path, textures)
                    val name = path.nameWithoutExtension
                    if (models.resolve("$relative$name.json").notExists()) {
                        val model = Identifier(namespace, "item/$relative$name")
                        contents.addFile(PackContents.modelsFilePath(model), model(model))
                    }
                    FileVisitResult.CONTINUE
                }
            }
        }
        if (models.isDirectory()) {
            models.visitFileTree {
                onVisitFile { path, _ ->
                    val relative = relativize(path, models)
                    val name = path.nameWithoutExtension
                    if (items.resolve("$relative$name.json").notExists()) {
                        definition(contents, namespace, relative, name)
                    }
                    FileVisitResult.CONTINUE
                }
            }
        }

        val directory = this.itemModelsPath(namespace) + "/"
        contents.forEach { path, _ ->
            if (path.startsWith(directory)) {
                val model = path.removePrefix(directory)
                val name = model.substringAfterLast('/')
                val relative = if (model.contains('/')) model.substringBeforeLast('/') + "/" else ""
                if ("${PackContents.itemsPath(namespace)}/$relative$name" !in contents) {
                    definition(contents, namespace, relative, name.removeSuffix(".json"))
                }
            }
        }
    }

    private fun itemTexturesPath(namespace: String): String {
        return "${PackContents.texturesPath(namespace)}/item"
    }

    private fun itemModelsPath(namespace: String): String {
        return "${PackContents.modelsPath(namespace)}/item"
    }

    private fun definition(contents: PackContents, namespace: String, relative: String, name: String) {
        val model = Identifier(namespace, "item/$relative$name")
        val item = Identifier(namespace, "$relative$name")
        contents.addFile(PackContents.itemFilePath(item), definition(model))
    }

    private fun relativize(path: Path, directory: Path): String {
        return (path.parent.toString() + "/").removePrefix("$directory/")
    }

    private fun model(texture: Identifier): String {
        return """
        {
          "parent": "minecraft:item/generated",
          "textures": {
            "layer0": "$texture"
          }
        }
        """.trimIndent()
    }

    private fun definition(model: Identifier): String {
        return """
        {
          "model": {
            "type": "minecraft:model",
            "model": "$model"
          }
        }
        """.trimIndent()
    }
}
