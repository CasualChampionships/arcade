package net.casual.arcade.model.pack

import com.mojang.serialization.JsonOps
import net.casual.arcade.model.TestModels
import net.casual.arcade.model.definition.ModelBounds
import net.casual.arcade.model.definition.ModelDefinition
import net.casual.arcade.model.definition.ModelTexture
import net.casual.arcade.utils.Identifier
import net.minecraft.SharedConstants
import net.minecraft.client.color.item.Dye
import net.minecraft.client.color.item.ItemTintSources
import net.minecraft.client.renderer.item.ClientItem
import net.minecraft.client.renderer.item.CuboidItemModelWrapper
import net.minecraft.client.renderer.item.ItemModels
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection
import net.minecraft.client.resources.model.cuboid.CuboidModel
import net.minecraft.client.resources.model.cuboid.UnbakedCuboidGeometry
import net.minecraft.resources.Identifier
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import java.awt.image.BufferedImage

class VanillaModelFormatTests {
    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrap() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
            ItemModels.bootstrap()
            ItemTintSources.bootstrap()
        }
    }

    @Test
    fun `bone models parse as cuboid models`() {
        for (model in TestModels.loadAll()) {
            val resources = ModelResources(model)
            for ((id, json) in resources.getModelJsons()) {
                val model = CuboidModel.fromStream(json.toString().reader())
                val geometry = assertInstanceOf<UnbakedCuboidGeometry>(model.geometry(), id.toString())
                assertFalse(geometry.elements().isEmpty(), id.toString())
                val slots = model.textureSlots().values()
                assertTrue(slots.containsKey("particle"), id.toString())
                for (element in geometry.elements()) {
                    for (face in element.faces().values) {
                        assertTrue(slots.containsKey(face.texture().removePrefix("#")), "$id references undefined texture ${face.texture()}")
                    }
                }
                assertEquals(180.0F, model.transforms()!!.head().rotation().y(), id.toString())
            }
        }
    }

    @Test
    fun `item definitions parse as client items`() {
        val resources = ModelResources(TestModels.load("robot"))
        for ((id, json) in resources.getItemJsons()) {
            val item = ClientItem.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow { AssertionError("$id: $it") }
            val wrapper = assertInstanceOf<CuboidItemModelWrapper.Unbaked>(item.model(), id.toString())
            assertEquals(id, wrapper.model())
            assertInstanceOf<Dye>(wrapper.tints().single())
        }
    }

    @Test
    fun `animated texture metadata parses`() {
        val image = BufferedImage(16, 48, BufferedImage.TYPE_INT_ARGB)
        val texture = ModelTexture("frames", image, 16, 48, 3)
        val definition = ModelDefinition.create(Identifier("test", "animated"), listOf(), mapOf(), listOf(texture), ModelBounds(1.0F, 1.0F))
        val resources = ModelResources(definition).getTextures()
        val meta = resources.single().meta!!
        val section = AnimationMetadataSection.CODEC.parse(JsonOps.INSTANCE, meta.get("animation"))
            .getOrThrow { AssertionError(it) }
        assertEquals(3, section.defaultFrameTime())
        assertEquals(16, section.calculateFrameSize(16, 48).height())
    }
}
