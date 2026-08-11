/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.gametest.mixins.fabric;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.gametest.fabric.InjectedTestSuites;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.stream.Stream;

@Mixin(targets = "net.fabricmc.fabric.impl.gametest.TestAnnotationLocator")
public class TestAnnotationLocatorMixin {
    @ModifyExpressionValue(
        method = "getTestMethods",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;stream()Ljava/util/stream/Stream;"
        )
    )
    private Stream<EntrypointContainer<?>> injectArcadeTestEntrypoints(Stream<EntrypointContainer<?>> original) {
        return Stream.concat(original, InjectedTestSuites.INSTANCE.getEntrypoints());
    }
}
