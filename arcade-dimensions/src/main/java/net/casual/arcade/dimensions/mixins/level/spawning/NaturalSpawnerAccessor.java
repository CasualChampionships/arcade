package net.casual.arcade.dimensions.mixins.level.spawning;

import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NaturalSpawner.class)
public interface NaturalSpawnerAccessor {
	@Accessor("MAGIC_NUMBER")
	static int getMagicNumber() {
		throw new AssertionError();
	}
}
