package net.casual.arcade.minigame.managers

import com.google.common.collect.HashMultimap
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
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
import net.casual.arcade.utils.PlayerUtils.markSilentRecipesDirty
import net.casual.arcade.utils.impl.ConcatenatedList.Companion.concat
import net.minecraft.network.protocol.game.ClientboundRecipePacket
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeInput
import net.minecraft.world.item.crafting.RecipeType
import java.util.*

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
    private val minigame: Minigame<*>
) {
    private val recipesByType = HashMultimap.create<RecipeType<*>, RecipeHolder<*>>()
    private val recipesById = Object2ObjectOpenHashMap<ResourceLocation, RecipeHolder<*>>()
    internal val players = HashMultimap.create<UUID, ResourceLocation>()

    init {
        this.minigame.events.register<PlayerClientboundPacketEvent>(1_000, flags = ListenerFlags.HAS_PLAYER) {
            this.onClientboundPacket(it)
        }
        this.minigame.events.register<MinigameAddPlayerEvent> { (_, player) ->
            player.connection.send(this.createRecipesPacket())
            player.markSilentRecipesDirty()
        }
        this.minigame.events.register<MinigameRemovePlayerEvent> {
            it.player.connection.send(ClientboundUpdateRecipesPacket(this.minigame.server.recipeManager.recipes))
        }
    }

    public fun add(recipe: RecipeHolder<*>) {
        this.recipesByType.put(recipe.value.type, recipe)
        this.recipesById[recipe.id] = recipe

        this.refreshRecipes()
    }

    public fun addAll(recipes: Collection<RecipeHolder<*>>) {
        for (recipe in recipes) {
            this.recipesByType.put(recipe.value.type, recipe)
            this.recipesById[recipe.id] = recipe
        }

        this.refreshRecipes()
    }

    public fun all(): Collection<RecipeHolder<*>> {
        return this.recipesByType.values()
    }

    public fun <I: RecipeInput, R: Recipe<I>> all(type: RecipeType<R>): Set<RecipeHolder<R>> {
        @Suppress("UNCHECKED_CAST")
        return this.recipesByType.get(type) as Set<RecipeHolder<R>>
    }

    public fun grant(player: ServerPlayer, recipes: Collection<RecipeHolder<*>>) {
        val awarded = ArrayList<ResourceLocation>()
        for (recipe in recipes) {
            if (this.players.put(player.uuid, recipe.id)) {
                awarded.add(recipe.id)
            }
        }
        if (awarded.isNotEmpty()) {
            player.connection.send(
                ClientboundRecipePacket(
                    ClientboundRecipePacket.State.ADD,
                    awarded,
                    listOf(),
                    player.recipeBook.bookSettings
                )
            )
        }
    }

    public fun grantSilently(player: ServerPlayer, recipes: Collection<RecipeHolder<*>>) {
        val mapped = recipes.map { it.id }
        this.players.putAll(player.uuid, mapped)
        player.markSilentRecipesDirty()
    }

    public fun grantAll(player: ServerPlayer) {
        this.grant(player, this.all())
    }

    public fun grantAllSilently(player: ServerPlayer) {
        this.grantSilently(player, this.all())
    }

    public fun revoke(player: ServerPlayer, recipes: Collection<RecipeHolder<*>>) {
        for (recipe in recipes) {
            this.players.remove(player.uuid, recipe.id)
        }
        player.connection.send(ClientboundRecipePacket(
            ClientboundRecipePacket.State.REMOVE,
            recipes.map { it.id },
            listOf(),
            player.recipeBook.bookSettings
        ))
    }

    public fun has(player: ServerPlayer, recipe: RecipeHolder<*>): Boolean {
        return this.players[player.uuid].contains(recipe.id)
    }

    public fun get(id: ResourceLocation): RecipeHolder<*>? {
        return this.recipesById[id]
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

    public fun <I: RecipeInput, R: Recipe<I>> findAll(
        type: RecipeType<R>,
        inventory: I,
        level: ServerLevel
    ): List<RecipeHolder<R>> {
        return this.all(type).stream()
            .filter { holder -> holder.value.matches(inventory, level) }
            .sorted(Comparator.comparing { holder -> holder.value.getResultItem(level.registryAccess()).descriptionId })
            .toList()
    }

    internal fun serialize(): JsonArray {
        val array = JsonArray()
        for ((player, recipes) in this.players.asMap()) {
            val json = JsonObject()
            json.addProperty("uuid", player.toString())
            json.add("recipes", recipes.toJsonStringArray { it.toString() })
            array.add(json)
        }
        return array
    }

    internal fun deserialize(array: JsonArray) {
        for (player in array.objects()) {
            val uuid = player.uuid("uuid")
            this.players.putAll(uuid, player.array("recipes").strings().map { ResourceLocation.parse(it) })
        }
    }

    private fun refreshRecipes() {
        val packet = this.createRecipesPacket()
        for (player in this.minigame.players) {
            player.connection.send(packet)
        }
    }

    private fun createRecipesPacket(): ClientboundUpdateRecipesPacket {
        val recipes = this.minigame.server.recipeManager.recipes.toList()
        val packet = ClientboundUpdateRecipesPacket(recipes.concat(this.all().toList()))
        return packet
    }

    private fun onClientboundPacket(event: PlayerClientboundPacketEvent) {
        val packet = event.packet
        if (packet is ClientboundUpdateRecipesPacket) {
            val recipes = this.all()
            if (packet.recipes.containsAll(recipes)) {
                return
            }

            event.packet = ClientboundUpdateRecipesPacket(packet.recipes.concat(recipes.toList()))
            return
        }

        if (packet is ClientboundRecipePacket && packet.state == ClientboundRecipePacket.State.INIT) {
            val unlocked = this.players[event.player.uuid]
            if (packet.recipes.containsAll(unlocked)) {
                return
            }

            event.packet = ClientboundRecipePacket(
                ClientboundRecipePacket.State.INIT,
                packet.recipes.concat(unlocked.toList()),
                packet.highlights,
                packet.bookSettings
            )
        }
    }
}