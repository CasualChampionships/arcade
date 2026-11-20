/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.type.ServerSideArgument
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.ServerPlayerSuggestionContext
import net.minecraft.commands.arguments.SerializableArgument
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument
import net.minecraft.commands.arguments.coordinates.WorldCoordinate
import net.minecraft.commands.arguments.coordinates.WorldCoordinates
import net.minecraft.commands.suggestions.SuggestionContext
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ChunkPos
import org.joml.Vector2i
import java.util.concurrent.CompletableFuture

/**
 * Argument type that allows specifying a region position
 * in the format of `<x> <z>`.
 */
public object RegionPosArgument: SerializableArgument.ContextFree<RegionPosArgument.WorldRegionCoordinates>, ServerSideArgument<RegionPosArgument.WorldRegionCoordinates> {
    override fun parse(reader: StringReader): WorldRegionCoordinates {
        val cursor = reader.cursor
        if (!reader.canRead()) {
            throw WorldCoordinates.ERROR_NOT_COMPLETE_2D.create()
        }

        val x = WorldCoordinate.parseInt(reader)
        if (!reader.canRead() || reader.peek() != ' ') {
            reader.cursor = cursor
            throw WorldCoordinates.ERROR_NOT_COMPLETE_2D.create()
        }
        reader.skip()
        val z = WorldCoordinate.parseInt(reader)
        return WorldRegionCoordinates(x, z)
    }

    override fun listServerSuggestions(
        context: CommandContext<out ServerPlayerSuggestionContext>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val pos = ChunkPos.containing(BlockPos.containing(context.source.position()))
        val coords = listOf(
            SuggestionContext.TextCoordinates("${pos.regionX}", "", "${pos.regionZ}")
        )
        return SuggestionContext.suggestXZCoordinates(
            builder.remaining, coords, builder, Commands.createValidator(this::parse)
        )
    }

    override fun asSerializableArgument(): SerializableArgument<*> {
        return ColumnPosArgument.INSTANCE
    }

    public data class WorldRegionCoordinates(
        val x: WorldCoordinate,
        val z: WorldCoordinate
    )

    @JvmStatic
    public fun region(): ArgumentType<WorldRegionCoordinates> {
        return this.toBrigadier()
    }

    @JvmStatic
    public fun getRegion(context: CommandContext<CommandSourceStack>, name: String): Vector2i {
        val coordinates = context.getArgument(name, WorldRegionCoordinates::class.java)
        val origin = ChunkPos.containing(BlockPos.containing(context.source.position))
        val x = coordinates.x.get(origin.regionX.toDouble()).toInt()
        val z = coordinates.z.get(origin.regionZ.toDouble()).toInt()
        return Vector2i(x, z)
    }
}