package net.casual.arcade.utils

import eu.pb4.polymer.resourcepack.api.ResourcePackCreator
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.network.ClientboundPacketEvent
import net.casual.arcade.events.network.PackStatusEvent
import net.casual.arcade.events.network.PlayerDisconnectEvent
import net.casual.arcade.font.BitmapFont
import net.casual.arcade.resources.PackInfo
import net.casual.arcade.resources.PackState
import net.casual.arcade.resources.PackStatus
import net.casual.arcade.resources.PlayerPackExtension
import net.fabricmc.loader.api.FabricLoader
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

public object ResourcePackUtils {
    // May be accessed off the main thread.
    // This is implemented like this since we cannot use PlayerExtensions.
    // Packs may be sent before the player has spawned in the world.
    private val universe = ConcurrentHashMap<UUID, PlayerPackExtension>()

    private val ServerPlayer.resourcePacks
        get() = getExtension(this.uuid)

    @JvmStatic
    public fun PackInfo.toPushPacket(): ClientboundResourcePackPushPacket {
        return ClientboundResourcePackPushPacket(this.uuid, this.url, this.hash, this.required, this.prompt)
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
    public fun ResourcePackCreator.addBitmapFont(font: BitmapFont) {
        this.creationEvent.register { builder ->
            builder.addData("assets/${font.id.namespace}/font/${font.id.path}.json", font.getData())
        }
    }

    @JvmStatic
    public fun ResourcePackCreator.addMissingItemModels(namespace: String) {
        val container = FabricLoader.getInstance().getModContainer(namespace).orElseThrow(::IllegalArgumentException)
        val assets = container.findPath("assets").orElseThrow(::FileNotFoundException)
        this.addMissingItemModels(namespace, assets)
    }

    @JvmStatic
    @OptIn(ExperimentalPathApi::class)
    private fun ResourcePackCreator.addMissingItemModels(namespace: String, assets: Path) {
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
                        val location = ResourceLocation(namespace, "item/$relative/$name")
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
    }
}