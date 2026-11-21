/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CommandBox.class)
public interface CommandBoxAccessor<C> {
    @Accessor("dispatcher")
    CommandDispatcher<C> arcade_getDispatcher();
}
