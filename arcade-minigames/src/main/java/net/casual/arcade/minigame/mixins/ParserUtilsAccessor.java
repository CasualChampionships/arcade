/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins;

import com.google.gson.stream.JsonReader;
import net.minecraft.commands.ParserUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ParserUtils.class)
public interface ParserUtilsAccessor {
	@Invoker("getPos")
	static int getReaderPos(JsonReader reader) {
		throw new AssertionError();
	}
}
