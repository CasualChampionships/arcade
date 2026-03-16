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
import java.util.concurrent.CompletableFuture

/**
 * Argument type that allows specifying a chunk position
 * in the format of `<x> <z>`.
 */
public class ChunkPosArgument: CustomArgumentType<ChunkPosArgument.WorldChunksCoordinates>() {
    override fun parse(reader: StringReader): WorldChunksCoordinates {
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
        return WorldChunksCoordinates(x, z)
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val source = context.source
        if (source !is CommandSourceStack) {
            return Suggestions.empty()
        }
        val pos = ChunkPos.containing(BlockPos.containing(source.position))
        val coords = listOf(
            TextCoordinates("${pos.x}", "", "${pos.z}")
        )
        return SharedSuggestionProvider.suggest2DCoordinates(
            builder.remaining, coords, builder, Commands.createValidator(this::parse)
        )
    }

    override fun getArgumentInfo(): CustomArgumentTypeInfo<*> {
        return CustomArgumentTypeInfo.of(ColumnPosArgument::class.java)
    }

    public data class WorldChunksCoordinates(
        val x: WorldCoordinate,
        val z: WorldCoordinate
    )

    public companion object {
        @JvmStatic
        public fun position(): ChunkPosArgument {
            return ChunkPosArgument()
        }

        @JvmStatic
        public fun getPosition(context: CommandContext<CommandSourceStack>, name: String): ChunkPos {
            val coordinates = context.getArgument(name, WorldChunksCoordinates::class.java)
            val origin = ChunkPos.containing(BlockPos.containing(context.source.position))
            val x = coordinates.x.get(origin.x.toDouble()).toInt()
            val z = coordinates.z.get(origin.z.toDouble()).toInt()
            return ChunkPos(x, z)
        }
    }
}