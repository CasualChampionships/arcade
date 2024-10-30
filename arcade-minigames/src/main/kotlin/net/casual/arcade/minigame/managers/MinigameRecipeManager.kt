package net.casual.arcade.minigame.managers

import com.google.common.collect.HashMultimap
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.casual.arcade.events.player.PlayerClientboundPacketEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.ListenerFlags
import net.casual.arcade.minigame.events.MinigameAddPlayerEvent
import net.casual.arcade.minigame.events.MinigameRemovePlayerEvent
import net.casual.arcade.utils.JsonUtils.array
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.JsonUtils.strings
import net.casual.arcade.utils.JsonUtils.toJsonStringArray
import net.casual.arcade.utils.JsonUtils.uuid
import net.casual.arcade.utils.impl.ConcatenatedList.Companion.concat
import net.minecraft.core.registries.Registries
import net.minecraft.network.protocol.game.ClientboundRecipeBookAddPacket
import net.minecraft.network.protocol.game.ClientboundRecipeBookRemovePacket
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.crafting.*
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry
import net.minecraft.world.item.crafting.display.RecipeDisplayId
import java.util.*
import kotlin.collections.ArrayList

/**
 * This class manages the recipes of a minigame.
 *
 * All recipes added to this manager are local to the
 * minigame only and do not exist outside the context
 * of the minigame.
 *
 * @see Minigame.recipes
 */
public class MinigameRecipeManager(
    private val minigame: Minigame
) {
    private val holdersWithEntries = ObjectArrayList<RecipeManager.ServerDisplayInfo>()
    private val recipeToEntries = Reference2ObjectOpenHashMap<ResourceKey<Recipe<*>>, List<RecipeDisplayEntry>>()

    private val recipesByType = HashMultimap.create<RecipeType<*>, RecipeHolder<*>>()
    private val recipesById = Reference2ObjectOpenHashMap<ResourceKey<Recipe<*>>, RecipeHolder<*>>()
    private val players = HashMultimap.create<UUID, ResourceKey<Recipe<*>>>()

    init {
        this.minigame.events.register<PlayerClientboundPacketEvent>(1_000, flags = ListenerFlags.HAS_PLAYER) {
            this.onClientboundPacket(it)
        }
        this.minigame.events.register<MinigameAddPlayerEvent>(this::onPlayerAdded)
        this.minigame.events.register<MinigameRemovePlayerEvent>(this::onPlayerRemoved)
    }

    public fun add(recipe: RecipeHolder<*>) {
        this.recipesByType.put(recipe.value.type, recipe)
        this.recipesById[recipe.id] = recipe

        this.addEntries(recipe)
    }

    public fun addAll(recipes: Collection<RecipeHolder<*>>) {
        for (recipe in recipes) {
            this.recipesByType.put(recipe.value.type, recipe)
            this.recipesById[recipe.id] = recipe

            this.addEntries(recipe)
        }
    }

    public fun all(): Collection<RecipeHolder<*>> {
        return this.recipesById.values
    }

    public fun <I: RecipeInput, R: Recipe<I>> all(type: RecipeType<R>): Set<RecipeHolder<R>> {
        @Suppress("UNCHECKED_CAST")
        return this.recipesByType.get(type) as Set<RecipeHolder<R>>
    }

    public fun grant(player: ServerPlayer, recipe: RecipeHolder<*>, silent: Boolean = false) {
        if (this.players.put(player.uuid, recipe.id)) {
            val entries = this.recipeToEntries[recipe.id]
            if (entries.isNullOrEmpty()) {
                return
            }
            player.connection.send(ClientboundRecipeBookAddPacket(
                this.mapToEntries(entries, silent), false
            ))
        }
    }

    public fun grantAll(
        player: ServerPlayer,
        recipes: Collection<RecipeHolder<*>> = this.all(),
        silent: Boolean = false
    ) {
        val awarded = ArrayList<RecipeDisplayEntry>()
        for (recipe in recipes) {
            if (this.players.put(player.uuid, recipe.id)) {
                val entries = this.recipeToEntries[recipe.id] ?: continue
                awarded.addAll(entries)
            }
        }
        if (awarded.isNotEmpty()) {
            player.connection.send(ClientboundRecipeBookAddPacket(
                this.mapToEntries(awarded, silent), false
            ))
        }
    }

    public fun revoke(player: ServerPlayer, recipe: RecipeHolder<*>) {
        if (this.players.remove(player.uuid, recipe.id)) {
            val entries = this.recipeToEntries[recipe.id]
            if (entries.isNullOrEmpty()) {
                return
            }
            player.connection.send(ClientboundRecipeBookRemovePacket(entries.map { it.id }))
        }
    }

    public fun revokeAll(player: ServerPlayer, recipes: Collection<RecipeHolder<*>> = this.all()) {
        val revoked = ArrayList<RecipeDisplayId>()
        for (recipe in recipes) {
            if (this.players.remove(player.uuid, recipe.id)) {
                val entries = this.recipeToEntries[recipe.id] ?: continue
                entries.mapTo(revoked) { it.id }
            }
        }
        player.connection.send(ClientboundRecipeBookRemovePacket(revoked))
    }

    public fun has(player: ServerPlayer, key: ResourceKey<Recipe<*>>): Boolean {
        return this.players[player.uuid].contains(key)
    }

    public fun get(id: ResourceKey<Recipe<*>>): RecipeHolder<*>? {
        return this.recipesById[id]
    }

    public fun getHolderWithEntry(id: Int): RecipeManager.ServerDisplayInfo? {
        return this.holdersWithEntries.getOrNull(-(id + 1))
    }

    public fun <I: RecipeInput, R: Recipe<I>> find(
        type: RecipeType<R>,
        input: I,
        level: ServerLevel
    ): Optional<RecipeHolder<R>> {
        return this.all(type).stream()
            .filter { holder -> holder.value.matches(input, level) }
            .findFirst()
    }

    internal fun serialize(): JsonArray {
        val array = JsonArray()
        for ((player, recipes) in this.players.asMap()) {
            val json = JsonObject()
            json.addProperty("uuid", player.toString())
            json.add("recipes", recipes.toJsonStringArray { it.location().toString() })
            array.add(json)
        }
        return array
    }

    internal fun deserialize(array: JsonArray) {
        for (player in array.objects()) {
            val uuid = player.uuid("uuid")
            this.players.putAll(uuid, player.array("recipes").strings().map {
                ResourceKey.create(Registries.RECIPE, ResourceLocation.parse(it))
            })
        }
    }

    private fun onPlayerAdded(event: MinigameAddPlayerEvent) {
        val player = event.player
        val entries = this.mapToEntries(this.getDisplaysForPlayer(player), false)
        player.connection.send(ClientboundRecipeBookAddPacket(entries, false))
    }

    private fun onPlayerRemoved(event: MinigameRemovePlayerEvent) {
        val player = event.player
        val ids = this.getDisplaysForPlayer(player).map { it.id }
        player.connection.send(ClientboundRecipeBookRemovePacket(ids))
    }

    private fun onClientboundPacket(event: PlayerClientboundPacketEvent) {
        val packet = event.packet
        if (packet is ClientboundRecipeBookAddPacket) {
            if (!packet.replace) {
                return
            }
            val entries = this.getDisplaysForPlayer(event.player)
            if (entries.isNotEmpty()) {
                val modified = packet.entries.concat(this.mapToEntries(entries, true))
                event.packet = ClientboundRecipeBookAddPacket(modified, true)
            }
        }
    }

    private fun mapToEntries(displays: List<RecipeDisplayEntry>, silent: Boolean): List<ClientboundRecipeBookAddPacket.Entry> {
        return displays.map { ClientboundRecipeBookAddPacket.Entry(it, !silent, false) }
    }

    private fun getDisplaysForPlayer(player: ServerPlayer): List<RecipeDisplayEntry> {
        val recipes = this.players[player.uuid] ?: return listOf()
        val unlocked = ArrayList<RecipeDisplayEntry>()
        for (recipe in recipes) {
            val entries = this.recipeToEntries[recipe] ?: continue
            unlocked.addAll(entries)
        }
        return unlocked
    }

    private fun addEntries(holder: RecipeHolder<*>) {
        val recipe = holder.value
        val entries = ObjectArrayList<RecipeDisplayEntry>(recipe.display().size)

        // TODO: We need to hook into the vanilla recipe groups!
        val groupId = if (recipe.group().isEmpty()) OptionalInt.empty() else OptionalInt.empty()
        val ingredients = if (recipe.isSpecial) Optional.empty() else Optional.of(recipe.placementInfo().ingredients())
        for (display in recipe.display()) {
            val id = -(this.holdersWithEntries.size + 1)
            val entry = RecipeDisplayEntry(RecipeDisplayId(id), display, groupId, recipe.recipeBookCategory(), ingredients)
            this.holdersWithEntries.add(RecipeManager.ServerDisplayInfo(entry, holder))
            entries.add(entry)
        }

        this.recipeToEntries[holder.id] = entries
    }
}