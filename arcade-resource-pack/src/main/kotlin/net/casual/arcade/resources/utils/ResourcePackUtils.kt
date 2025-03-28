/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.utils

import com.google.gson.JsonObject
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder
import eu.pb4.polymer.resourcepack.api.ResourcePackCreator
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.network.ClientboundPacketEvent
import net.casual.arcade.events.server.player.PlayerDisconnectEvent
import net.casual.arcade.events.server.player.PlayerDimensionChangeEvent
import net.casual.arcade.host.HostedPack
import net.casual.arcade.host.PackHost
import net.casual.arcade.host.pack.PathPack
import net.casual.arcade.resources.creator.NamedResourcePackCreator
import net.casual.arcade.resources.event.PackStatusEvent
import net.casual.arcade.resources.extensions.PlayerPackExtension
import net.casual.arcade.resources.font.FontResources
import net.casual.arcade.resources.pack.PackInfo
import net.casual.arcade.resources.pack.PackState
import net.casual.arcade.resources.pack.PackStatus
import net.casual.arcade.resources.sound.SoundResources
import net.casual.arcade.resources.utils.ShaderUtils.ColorReplacer
import net.casual.arcade.utils.JsonUtils
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import java.io.FileNotFoundException
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.*
import kotlin.reflect.KProperty

public object ResourcePackUtils {
    // May be accessed off the main thread.
    // This is implemented like this since we cannot use PlayerExtensions.
    // Packs may be sent before the player has spawned in the world.
    private val universe = ConcurrentHashMap<UUID, PlayerPackExtension>()

    private val ServerPlayer.resourcePacks
        get() = getExtension(this.uuid)

    @JvmStatic
    public fun PackInfo.toPushPacket(): ClientboundResourcePackPushPacket {
        return ClientboundResourcePackPushPacket(this.uuid, this.url, this.hash, this.required, Optional.ofNullable(this.prompt))
    }

    @JvmStatic
    public fun PackInfo.toPopPacket(): ClientboundResourcePackPopPacket {
        return ClientboundResourcePackPopPacket(Optional.of(this.uuid))
    }

    @JvmStatic
    public fun ServerPlayer.hasBeenSentPack(pack: PackInfo): Boolean {
        return this.getPackState(pack) != null
    }

    @JvmStatic
    public fun ServerPlayer.getAllPackStates(): Collection<PackState> {
        return this.resourcePacks.getAllPacks()
    }

    @JvmStatic
    public fun ServerPlayer.getPackState(pack: PackInfo): PackState? {
        return this.resourcePacks.getPackState(pack.uuid)
    }

    @JvmStatic
    public fun afterPacksLoad(players: Iterable<ServerPlayer>, block: () -> Unit) {
        getPackLoadingFuture(players).thenRun(block)
    }

    @JvmStatic
    public fun getPackLoadingFuture(players: Iterable<ServerPlayer>): CompletableFuture<Void> {
        return CompletableFuture.allOf(*players.map { it.getPackLoadingFuture() }.toTypedArray())
    }

    @JvmStatic
    public fun ServerPlayer.afterPacksLoad(block: () -> Unit) {
        this.getPackLoadingFuture().thenRun(block)
    }

    @JvmStatic
    public fun ServerPlayer.getPackLoadingFuture(): CompletableFuture<Void> {
        return this.resourcePacks.allLoadedFuture
    }

    @JvmStatic
    public fun ServerPlayer.sendResourcePack(pack: PackInfo, replace: Boolean = true): CompletableFuture<PackStatus> {
        val current = this.getPackState(pack)
        if (!replace && current != null) {
            if (current.isLoadingPack()) {
                return this.resourcePacks.addFuture(pack.uuid)
            }
            if (current.hasLoadedPack()) {
                return CompletableFuture.completedFuture(PackStatus.SUCCESS)
            }
        }

        this.connection.send(pack.toPushPacket())
        return this.resourcePacks.addFuture(pack.uuid)
    }

    @JvmStatic
    public fun ServerPlayer.removeResourcePack(pack: PackInfo): CompletableFuture<PackStatus> {
        this.connection.send(pack.toPopPacket())
        return this.resourcePacks.addFuture(pack.uuid)
    }

    @JvmStatic
    public fun ServerPlayer.removeAllResourcePacks() {
        this.connection.send(ClientboundResourcePackPopPacket(Optional.empty()))
    }

    /**
     * This converts the [HostedPack] to [PackInfo] to be able
     * to be sent to players on the server.
     *
     * @param required Whether the pack should be required for the player.
     * @param prompt The prompt given to the player.
     * @return The pack info.
     * @see PackInfo
     */
    public fun HostedPack.toPackInfo(required: Boolean = false, prompt: Component? = null): PackInfo {
        return PackInfo(this.url, this.hash, required, prompt)
    }

    public fun PackHost.HostedPackRef.toPackInfo(required: Boolean = false, prompt: Component? = null): PackInfoRef {
        return PackInfoRef(this, required, prompt)
    }

    public fun PackHost.addPack(path: Path, creator: NamedResourcePackCreator): PackHost.HostedPackRef {
        creator.buildTo(path)
        return this.addPack(PathPack(path.resolve(creator.zippedName())))
    }

    @JvmStatic
    public fun ResourcePackCreator.addLangsFromData(modid: String) {
        val container = FabricLoader.getInstance().getModContainer(modid).orElseThrow(::IllegalArgumentException)
        val data = container.findPath("data").orElseThrow(::FileNotFoundException)
        val langs = data.resolve(modid).resolve("lang")
        this.addLangsFrom(modid, langs)
    }

    @JvmStatic
    public fun ResourcePackCreator.addLangsFrom(namespace: String, langs: Path) {
        this.creationEvent.register { builder ->
            for (lang in langs.listDirectoryEntries()) {
                val translations = JsonUtils.decodeToJsonObject(lang.reader())
                mergeJsons(builder, "assets/${namespace}/lang/${lang.name}", translations)
            }
        }
    }

    @JvmStatic
    public fun ResourcePackCreator.addFont(id: ResourceLocation, generator: () -> String) {
        this.creationEvent.register { builder ->
            val fontDefinition = generator.invoke().encodeToByteArray()
            builder.addData("assets/${id.namespace}/font/${id.path}.json", fontDefinition)
        }
    }

    @JvmStatic
    public fun ResourcePackCreator.addFont(font: FontResources) {
        this.creationEvent.register { builder ->
            val fontDefinition = font.toJson().encodeToByteArray()
            builder.addData("assets/${font.id.namespace}/font/${font.id.path}.json", fontDefinition)
            for ((lang, translations) in font.getLangJsons()) {
                mergeJsons(builder, "assets/${font.id.namespace}/lang/${lang}.json", translations)
            }
        }
    }

    @JvmStatic
    public fun ResourcePackCreator.addSounds(sounds: SoundResources) {
        this.afterInitialCreationEvent.register { builder ->
            // We can only have 1 sounds.json
            val additional = JsonUtils.decodeToJsonObject(sounds.toJson())
            mergeJsons(builder, "assets/${sounds.namespace}/sounds.json", additional)
        }
    }

    @JvmStatic
    public fun ResourcePackCreator.addCustomOutlineColors(block: ColorReplacer.() -> Unit) {
        this.creationEvent.register { builder ->
            val replacer = ColorReplacer()
            replacer.block()
            val shader = ShaderUtils.getOutlineVertexShader(replacer.getMap(), replacer.getRainbow())
            builder.addData("assets/minecraft/shaders/core/rendertype_outline.vsh", shader.encodeToByteArray())
            if (replacer.getRainbow() != null) {
                builder.addData("assets/minecraft/shaders/core/rendertype_outline.json", ShaderUtils.getOutlineJsonShader().encodeToByteArray())
            }
        }
    }

    @JvmStatic
    public fun ResourcePackCreator.addMissingItemModels(namespace: String) {
        val container = FabricLoader.getInstance().getModContainer(namespace).orElseThrow(::IllegalArgumentException)
        val assets = container.findPath("assets").orElseThrow(::FileNotFoundException)
        this.addMissingItemModelsInternal(namespace, assets)
    }

    @JvmStatic
    public fun ResourcePackCreator.addMissingItemModels(namespace: String, source: Path) {
        this.addMissingItemModelsInternal(namespace, source.resolve("assets"))
    }

    @JvmStatic
    private fun ResourcePackCreator.addMissingItemModelsInternal(namespace: String, assets: Path) {
        val itemTextures = assets.resolve(namespace).resolve("textures").resolve("item")
        val itemModels = assets.resolve(namespace).resolve("models").resolve("item")
        val items = assets.resolve(namespace).resolve("items")
        val itemTexturesDirectory = "$itemTextures/"
        val itemModelsDirectory = "$itemModels/"
        this.creationEvent.register { builder ->
            if (itemTextures.isDirectory()) {
                itemTextures.visitFileTree {
                    onVisitFile { path, _ ->
                        tryAddMissingItemModel(namespace, path, itemTexturesDirectory, itemModels, builder)
                        FileVisitResult.CONTINUE
                    }
                }
            }
            if (itemModels.isDirectory()) {
                itemModels.visitFileTree {
                    onVisitFile { path, _ ->
                        tryAddMissingItemModelDefinitions(namespace, path, itemModelsDirectory, items, builder)
                        FileVisitResult.CONTINUE
                    }
                }
            }
            val modelsPath = "assets/$namespace/models/item/"
            builder.forEachFile { path, _ ->
                if (path.startsWith(modelsPath)) {
                    val definition = path.removePrefix(modelsPath)
                    val name = definition.substringAfterLast('/')
                    val relative = if (definition.contains('/')) definition.substringBeforeLast('/') + "/" else ""
                    if (builder.getData("assets/$namespace/items/$relative$name") == null) {
                        tryAddMissingItemModelDefinitionRaw(namespace, relative, name.removeSuffix(".json"), builder)
                    }
                }
            }
        }
    }

    private fun mergeJsons(builder: ResourcePackBuilder, path: String, additional: JsonObject) {
        val existing = builder.getData(path)
        val json = if (existing != null) {
            JsonUtils.decodeToJsonObject(existing.decodeToString())
        } else {
            JsonObject()
        }

        for ((string, element) in additional.entrySet()) {
            json.add(string, element)
        }

        builder.addData(path, JsonUtils.GSON.toJson(json).encodeToByteArray())
    }

    private fun tryAddMissingItemModelDefinitions(namespace: String, path: Path, dir: String, items: Path, builder: ResourcePackBuilder) {
        val name = path.nameWithoutExtension
        val relative = (path.parent.toString() + "/").removePrefix(dir)
        if (items.resolve("$relative$name.json").notExists()) {
            this.tryAddMissingItemModelDefinitionRaw(namespace, relative, name, builder)
        }
    }

    private fun tryAddMissingItemModelDefinitionRaw(namespace: String, relative: String, name: String, builder: ResourcePackBuilder) {
        val location = ResourceLocation.fromNamespaceAndPath(namespace, "item/$relative$name")
        builder.addData("assets/$namespace/items/$relative$name.json", getDefaultItemModelDefinition(location))
    }

    private fun tryAddMissingItemModel(namespace: String, path: Path, dir: String, models: Path, builder: ResourcePackBuilder) {
        val name = path.nameWithoutExtension
        val relative = (path.parent.toString() + "/").removePrefix(dir)
        val model = "$relative$name.json"
        if (models.resolve(model).notExists()) {
            val location = ResourceLocation.fromNamespaceAndPath(namespace, "item/$relative$name")
            builder.addData("assets/$namespace/models/item/$model", getDefaultItemModel(location))
        }
    }

    private fun getDefaultItemModelDefinition(location: ResourceLocation): ByteArray {
        return """
        {
          "model": {
            "type": "minecraft:model",
            "model": "$location"
          }
        }
        """.trimIndent().encodeToByteArray()
    }

    private fun getDefaultItemModel(location: ResourceLocation): ByteArray {
        return """
        {
          "parent": "minecraft:item/generated",
          "textures": {
            "layer0": "$location"
          }
        }
        """.trimIndent().encodeToByteArray()
    }

    private fun getExtension(uuid: UUID): PlayerPackExtension {
        return universe.getOrPut(uuid) { PlayerPackExtension(uuid) }
    }

    public class PackInfoRef(
        ref: PackHost.HostedPackRef,
        private val required: Boolean,
        private val prompt: Component?
    ) {
        private val hosted by ref

        public operator fun getValue(any: Any?, property: KProperty<*>): PackInfo {
            return this.hosted.toPackInfo(this.required, this.prompt)
        }
    }

    internal fun registerEvents() {
        GlobalEventHandler.Server.register<PlayerDisconnectEvent> { (_, profile) ->
            universe.remove(profile.id)
        }
        GlobalEventHandler.Server.register<ClientboundPacketEvent> { (_, profile, packet) ->
            // This may be off thread
            if (packet is ClientboundResourcePackPushPacket) {
                getExtension(profile.id).onPushPack(packet)
            } else if (packet is ClientboundResourcePackPopPacket) {
                getExtension(profile.id).onPopPack(packet)
            }
        }
        GlobalEventHandler.Server.register<PackStatusEvent> { (server, profile, uuid, status) ->
            getExtension(profile.id).onPackStatus(server, uuid, status)
        }
        GlobalEventHandler.Server.register<PlayerDimensionChangeEvent> { (player) ->
            for (pack in getExtension(player.uuid).getAllPacks()) {
                if (pack.isWaitingForResponse()) {
                    player.sendResourcePack(pack.info, true)
                }
            }
        }
    }
}