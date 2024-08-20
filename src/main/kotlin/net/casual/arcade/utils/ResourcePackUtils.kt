package net.casual.arcade.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import eu.pb4.polymer.resourcepack.api.ResourcePackCreator
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.network.ClientboundPacketEvent
import net.casual.arcade.events.network.PackStatusEvent
import net.casual.arcade.events.network.PlayerDisconnectEvent
import net.casual.arcade.events.player.PlayerDimensionChangeEvent
import net.casual.arcade.items.ItemModeller
import net.casual.arcade.resources.PackInfo
import net.casual.arcade.resources.PackState
import net.casual.arcade.resources.PackStatus
import net.casual.arcade.resources.extensions.PlayerPackExtension
import net.casual.arcade.resources.font.FontResources
import net.casual.arcade.resources.sound.SoundResources
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import java.io.FileNotFoundException
import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.*

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
    public fun ServerPlayer.afterPacksLoad(block: () -> Unit) {
        this.resourcePacks.allLoadedFuture.thenRunAsync(block, this.server)
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

    @JvmStatic
    public fun ResourcePackCreator.registerNextModel(item: Item, location: ResourceLocation): Int {
        val id = ItemModeller.getNextIdFor(item)
        @Suppress("UnstableApiUsage")
        this.forceDefineModel(item, id, location, true)
        return id
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
                builder.addData("assets/${namespace}/lang/${lang.name}", lang.readBytes())
            }
        }
    }

    @JvmStatic
    public fun ResourcePackCreator.addFont(font: FontResources) {
        this.creationEvent.register { builder ->
            builder.addData("assets/${font.id.namespace}/font/${font.id.path}.json", font.getJson().encodeToByteArray())
        }
    }

    @JvmStatic
    public fun ResourcePackCreator.addSounds(sounds: SoundResources) {
        this.afterInitialCreationEvent.register { builder ->
            // We can only have 1 sounds.json
            val path = "assets/${sounds.namespace}/sounds.json"
            val gson = Gson()
            val json = JsonObject()
            val existing = builder.getData(path)
            if (existing != null) {
                val existingJson = gson.fromJson(existing.decodeToString(), JsonObject::class.java)
                for ((string, element) in existingJson.entrySet()) {
                    json.add(string, element)
                }
            }

            val data = gson.fromJson(sounds.getJson(), JsonObject::class.java)
            for ((string, element) in data.entrySet()) {
                json.add(string, element)
            }

            builder.addData("assets/${sounds.namespace}/sounds.json", gson.toJson(json).encodeToByteArray())
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
    @OptIn(ExperimentalPathApi::class)
    private fun ResourcePackCreator.addMissingItemModelsInternal(namespace: String, assets: Path) {
        val itemTextures = assets.resolve(namespace).resolve("textures").resolve("item")
        val itemModels = assets.resolve(namespace).resolve("models")
        val itemTexturesDirectory = "$itemTextures/"
        this.creationEvent.register { builder ->
            itemTextures.visitFileTree {
                onVisitFile { path, _ ->
                    val name = path.nameWithoutExtension
                    val relative = path.parent.toString().removePrefix(itemTexturesDirectory)
                    val model = "$relative/$name.json"
                    if (itemModels.resolve(model).notExists()) {
                        val location = ResourceLocation.fromNamespaceAndPath(namespace, "item/$relative/$name")
                        builder.addData("assets/$namespace/models/$model", getDefaultItemModel(location))
                    }

                    FileVisitResult.CONTINUE
                }
            }
        }
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
        return this.universe.getOrPut(uuid) { PlayerPackExtension() }
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<PlayerDisconnectEvent> { (_, profile) ->
            this.universe.remove(profile.id)
        }
        GlobalEventHandler.register<ClientboundPacketEvent> { (_, profile, packet) ->
            // This may be off thread
            if (packet is ClientboundResourcePackPushPacket) {
                this.getExtension(profile.id).onPushPack(packet)
            } else if (packet is ClientboundResourcePackPopPacket) {
                this.getExtension(profile.id).onPopPack(packet)
            }
        }
        GlobalEventHandler.register<PackStatusEvent> { (_, profile, uuid, status) ->
            this.getExtension(profile.id).onPackStatus(uuid, status)
        }
        GlobalEventHandler.register<PlayerDimensionChangeEvent> { (player) ->
            for (pack in this.getExtension(player.uuid).getAllPacks()) {
                if (pack.isWaitingForResponse()) {
                    player.sendResourcePack(pack.info, true)
                }
            }
        }
    }
}