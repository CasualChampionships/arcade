package net.casual.arcade.area

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.casual.arcade.utils.serialization.BlockSerializer
import net.casual.arcade.utils.serialization.Vec3iSerializer
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

@Serializable
@SerialName("boxed")
public class BoxedAreaConfig(
    @Serializable(with = Vec3iSerializer::class)
    private val position: Vec3i = Vec3i.ZERO,
    private val radius: Int = 20,
    private val height: Int = 5,
    @Serializable(with = BlockSerializer::class)
    private val block: Block = Blocks.BARRIER
): PlaceableAreaConfig {
    override fun create(level: ServerLevel): PlaceableArea {
        return BoxedArea(this.position, this.radius, this.height, level, this.block)
    }

    public companion object {
        public val DEFAULT: BoxedAreaConfig = BoxedAreaConfig()
    }
}