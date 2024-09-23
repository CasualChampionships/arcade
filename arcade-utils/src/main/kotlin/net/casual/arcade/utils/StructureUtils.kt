package net.casual.arcade.utils

import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import java.nio.file.Path

public object StructureUtils {
    public fun read(path: Path, server: MinecraftServer = ServerUtils.getServer()): StructureTemplate {
        val structureNBT = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap())
        return server.structureManager.readStructure(structureNBT)
    }
}