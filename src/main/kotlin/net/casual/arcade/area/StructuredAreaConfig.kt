package net.casual.arcade.area

import com.google.gson.JsonObject
import net.casual.arcade.Arcade
import net.casual.arcade.area.*
import net.casual.arcade.utils.JsonUtils.int
import net.casual.arcade.utils.JsonUtils.set
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.StructureUtils
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.absolutePathString

public class StructuredAreaConfig(
    private val root: Path,
    private val path: String,
    private val position: Vec3i,
): PlaceableAreaConfig {
    override val id: String = "structured"

    private val structure by lazy {
        val path = this.root.resolve(this.path)
        try {
            StructureUtils.read(path)
        } catch (e: IOException) {
            Arcade.logger.error("Failed to read structured area config path ${path.absolutePathString()}")
            null
        }
    }

    override fun create(level: ServerLevel): PlaceableArea {
        val structure = this.structure ?: return BoxedAreaConfig.DEFAULT.create(level)
        return StructureArea(structure, this.position, level)
    }

    override fun write(): JsonObject {
        val json = JsonObject()
        json["path"] = this.path
        json["x"] = this.position.x
        json["y"] = this.position.y
        json["z"] = this.position.z
        return json
    }

    private class StructuredAreaConfigFactory(
        private val root: Path
    ): PlaceableAreaConfigFactory {
        override val id: String = "structured"

        override fun create(data: JsonObject): StructuredAreaConfig {
            val path = data.string("path")
            val x = data.int("x")
            val y = data.int("y")
            val z = data.int("z")
            return StructuredAreaConfig(this.root, path, Vec3i(x, y, z))
        }
    }

    public companion object {
        public fun factory(root: Path = Path.of(".")): PlaceableAreaConfigFactory {
            return StructuredAreaConfigFactory(root)
        }
    }
}