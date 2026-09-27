/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.mixins.advancement;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.casual.arcade.minigame.managers.MinigameAdvancementManager;
import net.minecraft.advancements.AdvancementTree;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AdvancementTree.class)
public class AdvancementTreeMixin {
    @WrapWithCondition(
        method = "addAll",
        at = @At(
            value = "INVOKE",
            target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V"
        )
    )
    private boolean checkIfOriginatedFromMinigame(Logger instance, String s, Object o) {
        return !MinigameAdvancementManager.SUPPRESS_LOGGING.orElse(false);
    }
}
