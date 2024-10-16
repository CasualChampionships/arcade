package net.casual.arcade.dimensions.mixins.level.spawning;

import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NaturalSpawner.SpawnState.class)
public interface SpawnStateAccessor {
	@Accessor("localMobCapCalculator")
	LocalMobCapCalculator getLocalMobCapCalculator();
}
