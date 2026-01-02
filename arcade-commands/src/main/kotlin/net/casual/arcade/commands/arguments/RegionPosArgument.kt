/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.type.CustomArgumentType
import net.casual.arcade.commands.type.CustomArgumentTypeInfo
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.SharedSuggestionProvider.TextCoordinates
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument
import net.minecraft.commands.arguments.coordinates.WorldCoordinate
import net.minecraft.core.BlockPos
import net.minecraft.world.level.ChunkPos
import org.joml.Vector2i
import java.util.concurrent.CompletableFuture

/**
 * Argument type that allows specifying a region position
 * in the format of `<x> <z>`.
 */
public class RegionPosArgument: CustomArgumentType<RegionPosArgument.WorldRegionCoordinates>() {
    override fun parse(reader: StringReader): WorldRegionCoordinates {
        val cursor = reader.cursor
        if (!reader.canRead()) {
            throw ColumnPosArgument.ERROR_NOT_COMPLETE.create()
        }

        val x = WorldCoordinate.parseInt(reader)
        if (!reader.canRead() || reader.peek() != ' ') {
            reader.cursor = cursor
            throw ColumnPosArgument.ERROR_NOT_COMPLETE.create()
        }
        reader.skip()
        val z = WorldCoordinate.parseInt(reader)
        return WorldRegionCoordinates(x, z)
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val source = context.source
        if (source !is CommandSourceStack) {
            return Suggestions.empty()
        }

        val pos = ChunkPos(BlockPos.containing(source.position))
        val coords = listOf(
            TextCoordinates("${pos.regionX}", "", "${pos.regionZ}")
        )
        return SharedSuggestionProvider.suggest2DCoordinates(
            builder.remaining, coords, builder, Commands.createValidator(this::parse)
        )
    }

    override fun getArgumentInfo(): CustomArgumentTypeInfo<*> {
        return CustomArgumentTypeInfo.of(ColumnPosArgument::class.java)
    }

    public data class WorldRegionCoordinates(
        val x: WorldCoordinate,
        val z: WorldCoordinate
    )

    public companion object {
        @JvmStatic
        public fun region(): RegionPosArgument {
            return RegionPosArgument()
        }

        @JvmStatic
        public fun getRegion(context: CommandContext<CommandSourceStack>, name: String): Vector2i {
            val coordinates = context.getArgument(name, WorldRegionCoordinates::class.java)
            val origin = ChunkPos(BlockPos.containing(context.source.position))
            val x = coordinates.x.get(origin.regionX.toDouble()).toInt()
            val z = coordinates.z.get(origin.regionZ.toDouble()).toInt()
            return Vector2i(x, z)
        }
    }
}