/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import com.mojang.datafixers.DataFixer
import com.mojang.serialization.Codec
import net.casual.arcade.utils.server.ServerSingleton
import net.minecraft.core.HolderGetter
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtUtils
import net.minecraft.server.MinecraftServer
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.*

public object StructureUtils {
    public fun read(path: Path, server: MinecraftServer = ServerSingleton.get()): StructureTemplate {
        val structureTag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap())
        return this.read(structureTag, fixer = server.fixerUpper)
    }

    public fun read(tag: CompoundTag, getter: HolderGetter<Block> = BuiltInRegistries.BLOCK, fixer: DataFixer? = null): StructureTemplate {
        val template = StructureTemplate()
        var fixed = tag
        if (fixer != null) {
            val version = NbtUtils.getDataVersion(tag, 500)
            fixed = DataFixTypes.STRUCTURE.updateToCurrentVersion(fixer, tag, version)
        }
        template.load(getter, fixed)
        return template
    }
}