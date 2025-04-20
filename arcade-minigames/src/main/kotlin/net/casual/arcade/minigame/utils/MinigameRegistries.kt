/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.utils

import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.chat.ChatFormatter
import net.casual.arcade.minigame.chat.PlayerChatFormatter
import net.casual.arcade.minigame.data.MinigameDataModule
import net.casual.arcade.minigame.managers.chat.MinigameChatMode
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.minigame.template.area.PlaceableAreaTemplate
import net.casual.arcade.minigame.template.minigame.MinigamesTemplate
import net.casual.arcade.minigame.template.teleporter.EntityTeleporter
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.registries.RegistryKeySupplier
import net.casual.arcade.utils.registries.RegistrySupplier
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

public object MinigameRegistryKeys: RegistryKeySupplier(ArcadeUtils.MOD_ID) {
    public val PLACEABLE_AREA_TEMPLATE: ResourceKey<Registry<MapCodec<out PlaceableAreaTemplate>>> = create("placeable_area_template")
    public val ENTITY_TELEPORTER: ResourceKey<Registry<MapCodec<out EntityTeleporter>>> = create("entity_teleporter")
    public val MINIGAME_CHAT_MODE: ResourceKey<Registry<MapCodec<out MinigameChatMode>>> = create("minigame_chat_mode")
    public val MINIGAMES_EVENT: ResourceKey<Registry<MapCodec<out MinigamesTemplate>>> = create("minigames_event")
    public val MINIGAME_FACTORY: ResourceKey<Registry<MapCodec<out MinigameFactory>>> = create("minigame_factory")
    public val MINIGAME_DATA_MODULE_PROVIDER: ResourceKey<Registry<MinigameDataModule.Provider>> = create("minigame_data_module_provider")
}

public object MinigameRegistries: RegistrySupplier() {
    public val PLACEABLE_AREA_TEMPLATE: Registry<MapCodec<out PlaceableAreaTemplate>> = create(MinigameRegistryKeys.PLACEABLE_AREA_TEMPLATE, PlaceableAreaTemplate::bootstrap)
    public val ENTITY_TELEPORTER: Registry<MapCodec<out EntityTeleporter>> = create(MinigameRegistryKeys.ENTITY_TELEPORTER, EntityTeleporter::bootstrap)
    public val MINIGAME_CHAT_MODES: Registry<MapCodec<out MinigameChatMode>> = create(MinigameRegistryKeys.MINIGAME_CHAT_MODE, MinigameChatMode::bootstrap)
    public val MINIGAME_FACTORY: Registry<MapCodec<out MinigameFactory>> = create(MinigameRegistryKeys.MINIGAME_FACTORY, MinigameFactory::bootstrap)
    public val MINIGAMES_EVENT: Registry<MapCodec<out MinigamesTemplate>> = create(MinigameRegistryKeys.MINIGAMES_EVENT, MinigamesTemplate::bootstrap)
    public val MINIGAME_DATA_MODULE_PROVIDER: Registry<MinigameDataModule.Provider> = create(MinigameRegistryKeys.MINIGAME_DATA_MODULE_PROVIDER, MinigameDataModule.Provider::bootstrap)
}