/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.npc.FakePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin {
    @ModifyExpressionValue(
        method = "attack",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/Entity;hurtMarked:Z",
            opcode = Opcodes.GETFIELD
        )
    )
    private boolean onResetDeltaMovement(boolean original, Entity target) {
        return target.hurtMarked && !(target instanceof FakePlayer);
    }
}
