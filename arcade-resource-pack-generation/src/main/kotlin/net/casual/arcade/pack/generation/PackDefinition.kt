/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.generation

import net.casual.arcade.pack.generation.utils.ZipPackWriter
import java.nio.file.Path
import java.util.UUID

/**
 * This class defines a resource pack which can be generated at
 * runtime. You provide [contents] which dynamically generate
 * the contents of the resource pack:
 *
 * ```kotlin
 * val MY_PACK: PackDefinition = PackDefinition("my_pack") {
 *     description = Component.literal("My pack")
 *
 *     include("mymod")
 *     file("assets/mymod/font/default.json", json)
 * }
 * ```
 *
 * @param name The name of the pack.
 * @param uuid The unique id of the pack, used to identify it when hosting.
 * @param contents The contents of the pack.
 * @see PackContents
 */
public class PackDefinition(
    public val name: String,
    public val uuid: UUID = UUID.randomUUID(),
    private val contents: PackContents.() -> Unit
) {
    public fun build(): ResourcePack {
        val contents = PackContents()
        contents.contents()
        val (bytes, hash) = ZipPackWriter.write(contents.finish())
        return ResourcePack(this.name, bytes, hash)
    }

    public fun buildTo(directory: Path): Path {
        return this.build().writeTo(directory)
    }
}
