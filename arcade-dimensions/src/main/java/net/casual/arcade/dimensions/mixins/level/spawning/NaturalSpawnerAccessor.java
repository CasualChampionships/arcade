/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
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
