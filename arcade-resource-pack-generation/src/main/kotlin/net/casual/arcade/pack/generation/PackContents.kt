/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.generation

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import net.casual.arcade.utils.JsonUtils
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.minecraft.SharedConstants
import net.minecraft.network.chat.Component
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.metadata.pack.PackMetadataSection
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.isSymbolicLink
import kotlin.io.path.readSymbolicLink

/**
 * @see PackDefinition
 */
public class PackContents internal constructor() {
    private val contents = Object2ObjectLinkedOpenHashMap<String, PackFile>()
    private val finishers = ArrayList<PackContents.() -> Unit>()
    private val metadata = JsonObject()

    public var description: Component = DEFAULT_DESCRIPTION

    public var icon: PackFile? = null

    public fun icon(path: Path) {
        this.icon = PackFile.of(path)
    }

    public fun icon(image: BufferedImage) {
        this.icon = PackFile.of(image)
    }

    public fun file(path: String, file: PackFile) {
        val normalized = path.trim('/')
        if (normalized == PACK_METADATA) {
            this.mcmeta(this.decode(file))
            return
        }

        val merger = this.mergerFor(normalized)
        if (merger == null) {
            this.contents[normalized] = file
            return
        }

        val existing = this.contents[normalized]
        if (existing !is Merged) {
            this.contents[normalized] = Merged(this.decode(file))
            return
        }
        when (merger) {
            Merger.Object -> this.merge(existing.json, this.decode(file))
            Merger.Sounds -> this.mergeSounds(existing.json, this.decode(file))
        }
    }

    public fun file(path: String, bytes: ByteArray) {
        this.file(path, PackFile.of(bytes))
    }

    public fun file(path: String, contents: String) {
        this.file(path, PackFile.of(contents))
    }

    public fun file(path: String, json: JsonElement) {
        this.file(path, PackFile.of(json))
    }

    public fun file(path: String, image: BufferedImage) {
        this.file(path, PackFile.of(image))
    }

    public fun get(path: String): PackFile? {
        return this.contents[path.trim('/')]
    }


    public operator fun contains(path: String): Boolean {
        return this.contents.containsKey(path.trim('/'))
    }

    public fun forEach(action: (path: String, file: PackFile) -> Unit) {
        for ((path, file) in this.contents.toList()) {
            action.invoke(path, file)
        }
    }

    public fun include(modid: String) {
        val container = FabricLoader.getInstance().getModContainer(modid).orElseThrow {
            IllegalArgumentException("Cannot include assets of mod '$modid', it is not loaded")
        }
        this.include(container)
    }

    public fun include(container: ModContainer) {
        for (root in container.rootPaths) {
            val directories = ArrayList<String>()
            directories.add(ASSETS)

            val mcmeta = root.resolve(PACK_METADATA)
            if (mcmeta.isRegularFile()) {
                val json = JsonUtils.decodeRaw<JsonObject>(mcmeta)
                this.mcmeta(json)
                directories.addAll(this.overlaysOf(json))
            }

            for (directory in directories) {
                val path = root.resolve(directory)
                if (path.isDirectory()) {
                    this.copy(path, directory)
                }
            }
        }
    }

    public fun include(path: Path) {
        this.copy(path, "")
    }

    public fun copy(source: Path, destination: String) {
        val resolved = if (source.isSymbolicLink()) source.readSymbolicLink() else source
        val prefix = destination.trim('/')

        if (resolved.isRegularFile()) {
            FileSystems.newFileSystem(resolved, emptyMap<String, Any>()).use { system ->
                for (root in system.rootDirectories) {
                    this.copyDirectory(root, prefix)
                }
            }
            return
        }
        if (!resolved.isDirectory()) {
            throw IllegalArgumentException("Cannot copy from $source, no such directory or zip exists")
        }
        this.copyDirectory(resolved, prefix)
    }

    public fun onFinish(callback: PackContents.() -> Unit) {
        this.finishers.add(callback)
    }

    internal fun finish(): Map<String, PackFile> {
        while (this.finishers.isNotEmpty()) {
            val finishers = ArrayList(this.finishers)
            this.finishers.clear()
            for (finisher in finishers) {
                this.finisher()
            }
        }

        val icon = this.icon
        if (icon != null) {
            this.contents[PACK_ICON] = icon
        }
        this.contents[PACK_METADATA] = PackFile.of(this.buildMcMeta())
        return this.contents
    }

    private fun copyDirectory(directory: Path, prefix: String) {
        Files.walk(directory).use { stream ->
            stream.forEach { path ->
                if (path.isRegularFile()) {
                    val relative = directory.relativize(path).toString().replace('\\', '/')
                    val destination = if (prefix.isEmpty()) relative else "$prefix/$relative"
                    this.file(destination, PackFile.of(Files.readAllBytes(path)))
                }
            }
        }
    }

    private fun buildMcMeta(): JsonObject {
        val format = SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES)
        val section = PackMetadataSection(this.description, format.minorRange())

        val json = JsonObject()
        for ((key, value) in this.metadata.entrySet()) {
            json.add(key, value)
        }
        json.add("pack", PackMetadataSection.CLIENT_TYPE.codec().encodeStart(JsonOps.INSTANCE, section).orThrow)
        return json
    }

    private fun mcmeta(json: JsonObject) {
        for ((key, value) in json.entrySet()) {
            if (key == "pack") {
                continue
            }
            val existing = this.metadata[key]
            if (existing is JsonObject && value is JsonObject) {
                merge(existing, value)
            } else {
                this.metadata.add(key, value)
            }
        }
    }

    private fun decode(file: PackFile): JsonObject {
        return JsonUtils.decodeRaw(file.bytes())
    }

    private fun mergerFor(path: String): Merger? {
        if (!path.endsWith(".json")) {
            return null
        }
        val split = path.split('/')
        val offset = when (ASSETS) {
            split.firstOrNull() -> 0
            split.getOrNull(1) -> 1
            else -> return null
        }
        val name = split.getOrNull(offset + 2) ?: return null
        return when (name) {
            "lang" if split.size == offset + 4 -> Merger.Object
            "atlases" -> Merger.Object
            "sounds.json" if split.size == offset + 3 -> Merger.Sounds
            else -> null
        }
    }

    private fun merge(target: JsonObject, source: JsonObject) {
        for ((key, value) in source.entrySet()) {
            when (val existing = target[key]) {
                is JsonObject if value is JsonObject -> this.merge(existing, value)
                is JsonArray if value is JsonArray -> existing.addAll(value)
                else -> target.add(key, value)
            }
        }
    }

    private fun mergeSounds(target: JsonObject, source: JsonObject) {
        for ((event, value) in source.entrySet()) {
            val existing = target[event]
            if (existing !is JsonObject || value !is JsonObject || value.replaces()) {
                target.add(event, value)
                continue
            }
            merge(existing, value)
        }
    }

    private fun overlaysOf(mcmeta: JsonObject): List<String> {
        val entries = mcmeta.getAsJsonObject("overlays")?.getAsJsonArray("entries") ?: return listOf()
        return entries.mapNotNull { entry ->
            (entry as? JsonObject)?.get("overlay")?.asString
        }
    }

    private enum class Merger {
        Object, Sounds
    }

    private class Merged(val json: JsonObject): PackFile {
        override fun stream(): InputStream {
            return ByteArrayInputStream(this.bytes())
        }

        override fun bytes(): ByteArray {
            return JsonUtils.MIN_GSON.toJson(this.json).encodeToByteArray()
        }
    }

    internal companion object {
        const val PACK_METADATA: String = "pack.mcmeta"
        const val PACK_ICON: String = "pack.png"
        const val ASSETS: String = "assets"

        val DEFAULT_DESCRIPTION: Component = Component.literal("Server Resource Pack")

        private fun JsonObject.replaces(): Boolean {
            return this.get("replace")?.asBoolean == true
        }
    }
}
