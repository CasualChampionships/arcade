package net.casual.arcade.model

import net.casual.arcade.model.definition.ModelDefinition
import net.casual.arcade.model.format.ModelLoadOptions
import net.casual.arcade.model.format.blockbench.BlockbenchModelLoader
import net.casual.arcade.utils.Identifier
import net.minecraft.resources.Identifier

internal object TestModels {
    const val ROBOT = "robot"
    const val SUNFLOWER = "sunflower"
    const val PIGEON ="pigeon"

    fun load(name: String, options: ModelLoadOptions = ModelLoadOptions.DEFAULT): ModelDefinition {
        val stream = TestModels::class.java.getResourceAsStream("/$name.bbmodel")
            ?: throw IllegalArgumentException("No such model named '$name' available")
        return stream.use { BlockbenchModelLoader.load(Identifier("test", name), it, options) }
    }

    fun loadInline(json: String): ModelDefinition {
        return BlockbenchModelLoader.load(Identifier("test", "inline"), json.byteInputStream())
    }

    fun loadAll(): List<ModelDefinition> {
        return listOf(ROBOT, SUNFLOWER, PIGEON).map(::load)
    }
}
