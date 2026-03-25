/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.mixins.datafix;

import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.filefix.FileFix;
import net.minecraft.util.filefix.fixes.DimensionStorageFileFix;
import net.minecraft.util.filefix.operations.FileFixOperations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DimensionStorageFileFix.class)
public abstract class DimensionStorageFileFixMixin extends FileFix {
    public DimensionStorageFileFixMixin(Schema schema) {
        super(schema);
    }

    @Inject(
        method = "makeFixer",
        at = @At("TAIL")
    )
    private void injectArcadeExtensionFixer(CallbackInfo ci) {
        this.addFileFixOperation(
            FileFixOperations.move("arcade/persistent-levels.nbt", "data/arcade/persistent_levels.dat")
        );
        this.addFileFixOperation(
            FileFixOperations.move("arcade/temporary-levels.nbt", "data/arcade/temporary_levels.dat")
        );
        this.addFileFixOperation(FileFixOperations.delete("arcade"));
    }
}

