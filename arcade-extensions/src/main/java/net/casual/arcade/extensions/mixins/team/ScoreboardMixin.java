/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.mixins.team;

import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.extensions.ExtensionHolder;
import net.casual.arcade.extensions.ducks.ArcadeTeamDataHolder;
import net.casual.arcade.utils.ArcadeUtils;
import net.casual.arcade.utils.ServerUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Scoreboard.class)
public class ScoreboardMixin {
    @Inject(
        method = "loadPlayerTeam",
        at = @At("TAIL")
    )
    private void onLoadPlayerTeam(
        PlayerTeam.Packed packed,
        CallbackInfo ci,
        @Local PlayerTeam team
    ) {
        ArcadeUtils.scopedProblemReporter(reporter -> {
            CompoundTag tag = Optional.ofNullable(((ArcadeTeamDataHolder) (Object) packed).arcade$getData())
                .orElseGet(CompoundTag::new);
            ValueInput input = TagValueInput.create(reporter, ServerUtils.getRegistryAccessOrEmpty(), tag);
            ExtensionHolder.deserialize((ExtensionHolder) team, input);
        });
    }
}
