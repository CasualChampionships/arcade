/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins.codec;

import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.util.ducks.GameRulesData;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRuleMap;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRules.class)
public class GameRuleMixin implements GameRulesData {
    @Unique private FeatureFlagSet enabledFeatures;

    @Shadow @Final private GameRuleMap rules;

    @Inject(
        method = {
            "<init>(Lnet/minecraft/world/flag/FeatureFlagSet;)V",
            "<init>(Lnet/minecraft/world/flag/FeatureFlagSet;Lnet/minecraft/world/level/gamerules/GameRuleMap;)V"
        },
        at = @At("TAIL")
    )
    private void saveFeatureFlagSet(
        CallbackInfo ci,
        @Local(argsOnly = true, name = "enabledFeatures") FeatureFlagSet enabledFeatures
    ) {
        this.enabledFeatures = enabledFeatures;
    }

    @Override
    public FeatureFlagSet arcade_getFeatureFlagSet() {
        return this.enabledFeatures;
    }

    @Override
    public GameRuleMap arcade_getGameRuleMap() {
        return this.rules;
    }
}
