/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.mixins.clock;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.world.clock.ServerClockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TimeCommand.class)
public class TimeCommandMixin {
    @ModifyExpressionValue(
        method = {
            "queryTime",
            "queryTimelineTicks",
            "queryTimelineRepetitions",
            "setTotalTicks",
            "addTime",
            "setTimeToTimeMarker",
            "setPaused",
            "setRate"
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;clockManager()Lnet/minecraft/world/clock/ServerClockManager;"
        )
    )
    private static ServerClockManager replaceWithLevelClockManager(
        ServerClockManager original,
        CommandSourceStack source
    ) {
        return source.getLevel().clockManager();
    }
}
