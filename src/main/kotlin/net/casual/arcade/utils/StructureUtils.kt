package net.casual.arcade.utils

import net.casual.arcade.Arcade
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import java.nio.file.Files
import java.nio.file.Path

public object StructureUtils {
    public fun read(path: Path): StructureTemplate {
        val structureNBT = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap())
        return Arcade.getServer().structureManager.readStructure(structureNBT)
    }
}