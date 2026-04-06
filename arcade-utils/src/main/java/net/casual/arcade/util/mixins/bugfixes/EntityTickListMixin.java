/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins.bugfixes;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTickList;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(EntityTickList.class)
public class EntityTickListMixin {
    // @Shadow private Int2ObjectMap<Entity> passive;

    @Inject(
        method = "forEach",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/level/entity/EntityTickList;iterated:Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;",
            opcode = Opcodes.PUTFIELD,
            ordinal = 1
        )
    )
    private void onFinishIteration(Consumer<Entity> output, CallbackInfo ci) {
        // I removed this 'fix' because I think it causes a compatibility issue
        // with async, pending testing

        // Fix a memory leak where entities don't get cleared from this map
        // this.passive.clear();
    }
}
