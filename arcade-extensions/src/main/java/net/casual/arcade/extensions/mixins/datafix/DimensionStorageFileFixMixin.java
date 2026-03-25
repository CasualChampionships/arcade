/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.mixins.datafix;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.datafixers.schemas.Schema;
import net.casual.arcade.utils.collection.CollectionUtilsKt;
import net.minecraft.util.filefix.FileFix;
import net.minecraft.util.filefix.fixes.DimensionStorageFileFix;
import net.minecraft.util.filefix.operations.FileFixOperations;
import net.minecraft.util.filefix.operations.Move;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(DimensionStorageFileFix.class)
public abstract class DimensionStorageFileFixMixin extends FileFix {
    public DimensionStorageFileFixMixin(Schema schema) {
        super(schema);
    }

    @Definition(id = "of", method = "Ljava/util/List;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;")
    @Definition(id = "moveSimple", method = "Lnet/minecraft/util/filefix/operations/FileFixOperations;moveSimple(Ljava/lang/String;)Lnet/minecraft/util/filefix/operations/Move;")
    @Expression("of(moveSimple('region'), moveSimple('entities'), moveSimple('poi'))")
    @ModifyExpressionValue(
        method = "makeFixer",
        at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private List<Move> injectArcadeExtensionFixer(List<Move> original) {
        return CollectionUtilsKt.concat(original, List.of(FileFixOperations.moveSimple("arcade-extension-data.nbt")));
    }
}

