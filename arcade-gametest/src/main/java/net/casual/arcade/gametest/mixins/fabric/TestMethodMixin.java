/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.gametest.mixins.fabric;

import net.casual.arcade.gametest.TestSuite;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

@Mixin(targets = "net.fabricmc.fabric.impl.gametest.TestAnnotationLocator$TestMethod")
public class TestMethodMixin {
    @Shadow @Final private EntrypointContainer<Object> entrypoint;

    @Shadow @Final private Method method;

    @Shadow
    private static String camelToSnake(String input) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Inject(
        method = "identifier",
        at = @At("HEAD"),
        cancellable = true
    )
    private void replaceIdentifierForTestSuite(CallbackInfoReturnable<Identifier> cir) {
        Object entrypoint = this.entrypoint.getEntrypoint();
        if (entrypoint instanceof TestSuite suite) {
            String namespace = suite.getNamespace().isBlank() ?
                this.entrypoint.getProvider().getMetadata().getId() : suite.getNamespace();
            String method = camelToSnake(this.method.getName().replace(' ', '_'));
            String path = suite.getPrefix().isBlank() ? method : suite.getPrefix() + "_" + method;
            cir.setReturnValue(Identifier.fromNamespaceAndPath(namespace, path));
        }
    }
}
