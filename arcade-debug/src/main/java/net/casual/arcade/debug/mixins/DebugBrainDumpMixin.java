/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.debug.mixins;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.casual.arcade.debug.behavior.BehaviorDescriptionOverrides;
import net.casual.arcade.debug.behavior.DebuggableNestedBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.debug.DebugBrainDump;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;
import java.util.stream.Stream;

@Mixin(DebugBrainDump.class)
public class DebugBrainDumpMixin {
    @Definition(id = "map", method = "Ljava/util/stream/Stream;map(Ljava/util/function/Function;)Ljava/util/stream/Stream;")
    @Definition(id = "debugString", method = "Lnet/minecraft/world/entity/ai/behavior/BehaviorControl;debugString()Ljava/lang/String;")
    @Expression("?.map(::debugString)")
    @WrapOperation(
        method = "takeBrainDump",
        at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private static Stream<String> mapBehavioursToDebugStrings(
        Stream<BehaviorControl<?>> stream,
        Function<BehaviorControl<?>, String> function,
        Operation<Stream<String>> original
    ) {
        return stream.flatMap(behavior -> {
            if (behavior instanceof DebuggableNestedBehavior nested) {
                return nested.debugStrings().stream();
            }
            return Stream.of(function.apply(behavior));
        });
    }

    @Inject(
        method = "getShortDescription",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void getShortDescriptionOverrides(
        ServerLevel level,
        @Nullable Object obj,
        CallbackInfoReturnable<String> cir
    ) {
        if (obj != null) {
            String override = BehaviorDescriptionOverrides.get(obj);
            if (override != null) {
                cir.setReturnValue(override);
            }
        }
    }
}
