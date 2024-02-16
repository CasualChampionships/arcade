package net.casual.arcade.resources

import eu.pb4.polymer.resourcepack.api.ResourcePackCreator
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

public class NamedResourcePackCreator(
    public val name: String,
    private val creator: ResourcePackCreator = ResourcePackCreator.create()
) {
    public fun getCreator(): ResourcePackCreator {
        return this.creator
    }

    public fun buildTo(path: Path) {
        if (path.exists() && !path.isDirectory()) {
            throw IllegalArgumentException("Must specify directory when building NamedResourcePack")
        }
        path.createDirectories()
        val name = if (this.name.endsWith(".zip")) this.name else "${this.name}.zip"
        this.creator.build(path.resolve(name))
    }

    public companion object {
        public fun named(name: String, block: ResourcePackCreator.() -> Unit): NamedResourcePackCreator {
            val creator = ResourcePackCreator.create()
            block(creator)
            return NamedResourcePackCreator(name, creator)
        }
    }
}