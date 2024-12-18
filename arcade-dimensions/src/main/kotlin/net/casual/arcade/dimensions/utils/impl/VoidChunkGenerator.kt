/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.utils.impl

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.*
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.WorldGenRegion
import net.minecraft.util.random.WeightedRandomList
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.*
import net.minecraft.world.level.biome.*
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.RandomState
import net.minecraft.world.level.levelgen.blending.Blender
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureSet
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager
import java.util.concurrent.CompletableFuture
import java.util.stream.Stream

/**
 * A [ChunkGenerator] implementation which creates a void world.
 *
 * @param biome The biome source.
 */
public class VoidChunkGenerator(biome: BiomeSource): ChunkGenerator(biome) {
    /**
     * @param biome The desired biome holder.
     */
    public constructor(biome: Holder<Biome>): this(FixedBiomeSource(biome))

    /**
     * @param server The [MinecraftServer] instance.
     * @param key The key for the desired biome.
     */
    public constructor(server: MinecraftServer, key: ResourceKey<Biome>):
        this(server.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(key))

    /**
     * Creates an instance of this class with the [Biomes.THE_VOID] biome.
     *
     * @param server The [MinecraftServer] instance.
     */
    public constructor(server: MinecraftServer): this(server, Biomes.THE_VOID)

    override fun codec(): MapCodec<out ChunkGenerator> {
        return CODEC
    }

    override fun createState(lookup: HolderLookup<StructureSet>, randomState: RandomState, seed: Long): ChunkGeneratorStructureState {
        return ChunkGeneratorStructureState.createForFlat(randomState, seed, this.biomeSource, Stream.empty())
    }

    override fun applyCarvers(worldGenRegion: WorldGenRegion, l: Long, randomState: RandomState, biomeManager: BiomeManager, structureManager: StructureManager, chunkAccess: ChunkAccess) {

    }

    override fun findNearestMapStructure(level: ServerLevel, structure: HolderSet<Structure>, pos: BlockPos, searchRadius: Int, skipKnownStructures: Boolean): Pair<BlockPos, Holder<Structure>>? {
        return null
    }

    override fun applyBiomeDecoration(level: WorldGenLevel, chunk: ChunkAccess, structureManager: StructureManager) {

    }

    override fun buildSurface(level: WorldGenRegion, manager: StructureManager, random: RandomState, chunk: ChunkAccess) {

    }

    override fun spawnOriginalMobs(level: WorldGenRegion) {

    }

    override fun getGenDepth(): Int {
        return 0
    }

    override fun getMobsAt(biome: Holder<Biome>, structureManager: StructureManager, category: MobCategory, pos: BlockPos): WeightedRandomList<MobSpawnSettings.SpawnerData> {
        return WeightedRandomList.create()
    }

    override fun createStructures(registryAccess: RegistryAccess, structureState: ChunkGeneratorStructureState, structureManager: StructureManager, chunk: ChunkAccess, structureTemplateManager: StructureTemplateManager, dimension: ResourceKey<Level>) {
        super.createStructures(registryAccess, structureState, structureManager, chunk, structureTemplateManager, dimension)
    }

    override fun createReferences(level: WorldGenLevel, structureManager: StructureManager, chunk: ChunkAccess) {

    }

    override fun fillFromNoise(blender: Blender, randomState: RandomState, manager: StructureManager, chunk: ChunkAccess): CompletableFuture<ChunkAccess> {
        return CompletableFuture.completedFuture(chunk)
    }

    override fun getSeaLevel(): Int {
        return 0
    }

    override fun getMinY(): Int {
        return 0
    }

    override fun getBaseHeight(x: Int, z: Int, type: Heightmap.Types, level: LevelHeightAccessor, random: RandomState): Int {
        return 0
    }

    override fun getBaseColumn(x: Int, z: Int, height: LevelHeightAccessor, random: RandomState): NoiseColumn {
        return NoiseColumn(0, emptyArray())
    }

    override fun addDebugScreenInfo(info: MutableList<String>, random: RandomState, pos: BlockPos) {

    }

    public companion object {
        public val CODEC: MapCodec<VoidChunkGenerator> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                BiomeSource.CODEC.fieldOf("biome_source").forGetter(VoidChunkGenerator::getBiomeSource)
            ).apply(instance, ::VoidChunkGenerator)
        }
    }
}