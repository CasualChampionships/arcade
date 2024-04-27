package net.casual.arcade.area

import com.google.gson.JsonObject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.casual.arcade.Arcade
import net.casual.arcade.utils.JsonUtils.int
import net.casual.arcade.utils.JsonUtils.set
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.StructureUtils
import net.casual.arcade.utils.serialization.PathSerializer
import net.casual.arcade.utils.serialization.Vec3iSerializer
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.absolutePathString

@Serializable
@SerialName("structured")
public class StructuredAreaConfig(
    @Serializable(with = PathSerializer::class)
    private val path: Path,
    @Serializable(with = Vec3iSerializer::class)
    private val position: Vec3i,
): PlaceableAreaConfig {
    private val structure by lazy {
        try {
            StructureUtils.read(this.path)
        } catch (e: IOException) {
            Arcade.logger.error("Failed to read structured area config path ${this.path.absolutePathString()}")
            null
        }
    }

    override fun create(level: ServerLevel): PlaceableArea {
        val structure = this.structure ?: return BoxedAreaConfig.DEFAULT.create(level)
        return StructureArea(structure, this.position, level)
    }
}