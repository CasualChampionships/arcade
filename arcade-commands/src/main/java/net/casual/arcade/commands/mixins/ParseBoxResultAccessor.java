/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands.mixins;

import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.ParseBox;
import net.minecraft.commands.ParseContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ParseBox.Result.class)
public interface ParseBoxResultAccessor {
    @Accessor("parseResults")
    ParseResults<ParseContext> arcade_getParseResults();
}
