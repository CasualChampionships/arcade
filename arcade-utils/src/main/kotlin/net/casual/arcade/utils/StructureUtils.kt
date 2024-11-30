package net.casual.arcade.utils

import com.mojang.serialization.Codec
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.*

public object StructureUtils {
    public fun read(path: Path, server: MinecraftServer = ServerUtils.getServer()): StructureTemplate {
        val structureTag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap())
        return server.structureManager.readStructure(structureTag)
    }

    public fun <A> readWithData(
        path: Path,
        codec: Codec<A>,
        server: MinecraftServer = ServerUtils.getServer()
    ): Pair<StructureTemplate, A> {
        if (path.isDirectory()) {
            return this.readDirectoryWithData(path, codec, server)
        }
        return this.readZippedWithData(path, codec, server)
    }

    private fun <A> readDirectoryWithData(
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
        val data = dataPath.inputStream().use { stream ->
            JsonUtils.decodeWith(codec, stream)
        }.orThrow
        return read(structurePath, server) to data
    }

    private fun <A> readZippedWithData(
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
            val data = zip.getInputStream(dataEntry).use { stream ->
                JsonUtils.decodeWith(codec, stream)
            }.orThrow
            return server.structureManager.readStructure(structureTag) to data
        }
    }
}