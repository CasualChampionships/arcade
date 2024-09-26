package net.casual.arcade.dimensions.utils.impl

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.HolderSet
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.WorldGenRegion
import net.minecraft.world.level.LevelHeightAccessor
import net.minecraft.world.level.NoiseColumn
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.biome.BiomeSource
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.FixedBiomeSource
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.RandomState
import net.minecraft.world.level.levelgen.blending.Blender
import net.minecraft.world.level.levelgen.structure.Structure
import java.util.concurrent.CompletableFuture

public class VoidChunkGenerator(biome: BiomeSource): ChunkGenerator(biome) {
    public constructor(biome: Holder<Biome>): this(FixedBiomeSource(biome))

    public constructor(server: MinecraftServer, key: ResourceKey<Biome>):
        this(server.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(key))

    public constructor(server: MinecraftServer): this(server, Biomes.THE_VOID)

    override fun codec(): MapCodec<out ChunkGenerator> {
        return CODEC
    }

    override fun applyCarvers(level: WorldGenRegion, seed: Long, random: RandomState, biomeManager: BiomeManager, structureManager: StructureManager, chunk: ChunkAccess, step: GenerationStep.Carving) {

    }

    override fun findNearestMapStructure(level: ServerLevel, structure: HolderSet<Structure>, pos: BlockPos, searchRadius: Int, skipKnownStructures: Boolean): Pair<BlockPos, Holder<Structure>>? {
        return null
    }

    override fun buildSurface(level: WorldGenRegion, manager: StructureManager, random: RandomState, chunk: ChunkAccess) {

    }

    override fun spawnOriginalMobs(level: WorldGenRegion) {

    }

    override fun getGenDepth(): Int {
        return 0
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