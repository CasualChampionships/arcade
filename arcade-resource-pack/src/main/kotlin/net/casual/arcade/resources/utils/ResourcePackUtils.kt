package net.casual.arcade.resources.utils

import com.google.gson.JsonObject
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder
import eu.pb4.polymer.resourcepack.api.ResourcePackCreator
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.network.ClientboundPacketEvent
import net.casual.arcade.events.network.PlayerDisconnectEvent
import net.casual.arcade.events.player.PlayerDimensionChangeEvent
import net.casual.arcade.host.HostedPack
import net.casual.arcade.host.PackHost
import net.casual.arcade.host.pack.PathPack
import net.casual.arcade.resources.creator.NamedResourcePackCreator
import net.casual.arcade.resources.pack.PackInfo
import net.casual.arcade.resources.pack.PackState
import net.casual.arcade.resources.pack.PackStatus
import net.casual.arcade.resources.event.PackStatusEvent
import net.casual.arcade.resources.extensions.PlayerPackExtension
import net.casual.arcade.resources.font.FontResources
import net.casual.arcade.resources.sound.SoundResources
import net.casual.arcade.resources.utils.ResourcePackUtils.toPackInfo
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
        return universe.getOrPut(uuid) { PlayerPackExtension() }
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
        GlobalEventHandler.register<PlayerDisconnectEvent> { (_, profile) ->
            universe.remove(profile.id)
        }
        GlobalEventHandler.register<ClientboundPacketEvent> { (_, profile, packet) ->
            // This may be off thread
            if (packet is ClientboundResourcePackPushPacket) {
                getExtension(profile.id).onPushPack(packet)
            } else if (packet is ClientboundResourcePackPopPacket) {
                getExtension(profile.id).onPopPack(packet)
            }
        }
        GlobalEventHandler.register<PackStatusEvent> { (_, profile, uuid, status) ->
            getExtension(profile.id).onPackStatus(uuid, status)
        }
        GlobalEventHandler.register<PlayerDimensionChangeEvent> { (player) ->
            for (pack in getExtension(player.uuid).getAllPacks()) {
                if (pack.isWaitingForResponse()) {
                    player.sendResourcePack(pack.info, true)
                }
            }
        }
    }
}