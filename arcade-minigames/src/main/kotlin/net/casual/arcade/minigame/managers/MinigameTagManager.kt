package net.casual.arcade.minigame.managers

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.MinigameAddTagEvent
import net.casual.arcade.minigame.events.MinigameRemoveTagEvent
import net.casual.arcade.utils.JsonUtils.array
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.JsonUtils.set
import net.casual.arcade.utils.JsonUtils.strings
import net.casual.arcade.utils.JsonUtils.uuid
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import java.util.*

public class MinigameTagManager(
    private val minigame: Minigame<*>
) {
    private val players = Object2ObjectOpenHashMap<UUID, MutableSet<ResourceLocation>>()

    public fun get(player: ServerPlayer): Collection<ResourceLocation> {
        return this.players[player.uuid] ?: listOf()
    }

    public fun has(player: ServerPlayer, tag: ResourceLocation): Boolean {
        return this.players[player.uuid]?.contains(tag) ?: false
    }

    public fun add(player: ServerPlayer, tag: ResourceLocation): Boolean {
        val result = this.players.getOrPut(player.uuid) { ObjectOpenHashSet() }.add(tag)
        if (result) {
            GlobalEventHandler.broadcast(MinigameAddTagEvent(this.minigame, player, tag))
        }
        return result
    }

    public fun remove(player: ServerPlayer, tag: ResourceLocation): Boolean {
        val result = this.players[player.uuid]?.remove(tag) ?: false
        if (result) {
            GlobalEventHandler.broadcast(MinigameRemoveTagEvent(this.minigame, player, tag))
        }
        return result
    }

    internal fun serialize(): JsonArray {
        val array = JsonArray()
        for ((uuid, tags) in this.players) {
            val json = JsonObject()
            json["uuid"] = uuid.toString()
            val tagArray = JsonArray()
            for (tag in tags) {
                tagArray.add(tag.toString())
            }
            json["tags"] = tagArray
            array.add(json)
        }
        return array
    }

    internal fun deserialize(array: JsonArray) {
        for (json in array.objects()) {
            val uuid = json.uuid("uuid")
            val tags = json.array("tags").strings()
            val set = this.players.getOrPut(uuid) { ObjectOpenHashSet() }
            for (tag in tags) {
                set.add(ResourceLocation.parse(tag))
            }
        }
    }
}