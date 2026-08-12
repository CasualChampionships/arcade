/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.gametest.mixins;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.DataFixer;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.loader.ResourceManagerTemplateSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.loader.TemplateSource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureTemplateManager.class)
public class StructureTemplateManagerMixin {
    @Unique private static final FileToIdConverter arcade_structureConverter = new FileToIdConverter("gametest/structure", ".nbt");

    @Unique
    private @Nullable ResourceManagerTemplateSource arcade_structureSource;

    @Inject(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/google/common/collect/ImmutableList$Builder;build()Lcom/google/common/collect/ImmutableList;"
        )
    )
    private void addGameTestNbtSource(
        ResourceManager resourceManager,
        LevelStorageSource.LevelStorageAccess storage,
        DataFixer fixerUpper,
        HolderGetter<Block> blockLookup,
        CallbackInfo ci,
        @Local(name = "sources") ImmutableList.Builder<TemplateSource> sources
    ) {
        this.arcade_structureSource = new ResourceManagerTemplateSource(fixerUpper, blockLookup, resourceManager, arcade_structureConverter);
        sources.add(this.arcade_structureSource);
    }

    @Inject(
        method = "onResourceManagerReload",
        at = @At("HEAD")
    )
    private void onReloadGameTestNbtSource(ResourceManager resourceManager, CallbackInfo ci) {
        if (this.arcade_structureSource != null) {
            this.arcade_structureSource.setResourceManager(resourceManager);
        }
    }
}
