package net.casual.arcade.resources.creator

import eu.pb4.polymer.resourcepack.api.ResourcePackCreator
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

public class NamedResourcePackCreator private constructor(
    private val name: String,
    private val creator: ResourcePackCreator = ResourcePackCreator.create()
) {
    public fun zippedName(): String {
        return if (this.name.endsWith(".zip")) this.name else "${this.name}.zip"
    }

    public fun getCreator(): ResourcePackCreator {
        return this.creator
    }

    public fun buildTo(path: Path) {
        if (path.exists() && !path.isDirectory()) {
            throw IllegalArgumentException("Must specify directory when building NamedResourcePack")
        }
        path.createDirectories()
        this.creator.build(path.resolve(this.zippedName()))
    }

    public companion object {
        public fun named(name: String, block: ResourcePackCreator.() -> Unit = {}): NamedResourcePackCreator {
            val creator = ResourcePackCreator.create()
            creator.block()
            return NamedResourcePackCreator(name, creator)
        }
    }
}