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

    @Deprecated("You should be using minigame modules instead")
    public fun <A: Any> readWithData(
        path: Path,
        codec: Codec<A>,
        server: MinecraftServer = ServerSingleton.get()
    ): Pair<StructureTemplate, A> {
        if (path.isDirectory()) {
            return this.readDirectoryWithData(path, codec, server)
        }
        return this.readZippedWithData(path, codec, server)
    }

    private fun <A: Any> readDirectoryWithData(
        path: Path,
        codec: Codec<A>,
        server: MinecraftServer
    ): Pair<StructureTemplate, A> {
        if (!path.isDirectory()) {
            throw IllegalArgumentException("Expected directory!")
        }
        val structurePath = path.resolve("structure.nbt")
        val dataPath = path.resolve("data.json")
        if (structurePath.notExists()) {
            throw IllegalArgumentException("Missing 'structure.nbt' in '$path'")
        }
        if (dataPath.notExists()) {
            throw IllegalArgumentException("Missing 'data.json' in '$path'")
        }
        val data = JsonUtils.decodeWith(codec, dataPath, null).orThrow
        return read(structurePath, server) to data
    }

    private fun <A: Any> readZippedWithData(
        path: Path,
        codec: Codec<A>,
        server: MinecraftServer
    ): Pair<StructureTemplate, A> {
        val resolved = if (path.extension.isEmpty()) path.resolveSibling("${path.nameWithoutExtension}.zip") else path
        if (resolved.extension != "zip") {
            throw IllegalArgumentException("Structure zip '$resolved' must be a zip file!")
        }
        if (!resolved.isReadable()) {
            throw IllegalArgumentException("Cannot read '$resolved'")
        }
        ZipFile(resolved.toFile()).use { zip ->
            val structureEntry = zip.getEntry("structure.nbt")
                ?: throw IllegalArgumentException("Missing 'structure.nbt' in '$resolved'")
            val dataEntry = zip.getEntry("data.json")
                ?: throw IllegalArgumentException("Missing 'data.json' in '$resolved'")
            val structureTag = zip.getInputStream(structureEntry).use { stream ->
                NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap())
            }
            val data = zip.getInputStream(dataEntry).reader().use { reader ->
                JsonUtils.decodeWith(codec, reader)
            }.orThrow
            return this.read(structureTag, fixer = server.fixerUpper) to data
        }
    }
}