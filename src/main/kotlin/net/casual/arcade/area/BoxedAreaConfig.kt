package net.casual.arcade.area

import com.google.gson.JsonObject
import net.casual.arcade.utils.JsonUtils.int
import net.casual.arcade.utils.JsonUtils.set
import net.casual.arcade.utils.JsonUtils.string
import net.minecraft.core.Vec3i
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

public class BoxedAreaConfig(
    private val position: Vec3i,
    private val radius: Int,
    private val height: Int,
    private val block: Block
): PlaceableAreaConfig {
    override val id: String = BoxedAreaConfig.id

    override fun create(level: ServerLevel): PlaceableArea {
        return BoxedArea(this.position, this.radius, this.height, level, this.block)
    }

    override fun write(): JsonObject {
        val json = JsonObject()
        json["x"] = this.position.x
        json["y"] = this.position.y
        json["z"] = this.position.z
        json["radius"] = this.radius
        json["height"] = this.height
        json["block"] = BuiltInRegistries.BLOCK.getKey(this.block).toString()
        return json
    }

    public companion object: PlaceableAreaConfigFactory {
        public val DEFAULT: BoxedAreaConfig = BoxedAreaConfig(Vec3i.ZERO, 20, 5, Blocks.BARRIER)

        override val id: String = "boxed"

        override fun create(data: JsonObject): PlaceableAreaConfig {
            val x = data.int("x")
            val y = data.int("y")
            val z = data.int("z")
            val radius = data.int("radius")
            val height = data.int("height")
            val block = BuiltInRegistries.BLOCK.get(ResourceLocation(data.string("block")))
            return BoxedAreaConfig(Vec3i(x, y, z), radius, height, block)
        }
    }
}