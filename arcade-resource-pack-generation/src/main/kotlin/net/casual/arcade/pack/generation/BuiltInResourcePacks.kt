/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.generation

import net.casual.arcade.pack.font.padding.PaddingNoSplitFontResources
import net.casual.arcade.pack.font.padding.PaddingSplitFontResources
import net.casual.arcade.pack.font.pixel.PixelFontResources
import net.casual.arcade.pack.font.spacing.SpacingFontResources
import net.casual.arcade.pack.generation.utils.BuiltInFontDefinitions
import net.casual.arcade.pack.generation.utils.ShaderUtils
import net.casual.arcade.pack.utils.BuiltInFonts
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component
import java.nio.file.Path

/**
 * Contains some commonly used resource packs.
 */
public object BuiltInResourcePacks {
    private val container = FabricLoader.getInstance().getModContainer("arcade-resource-pack-generation").get()

    public val ACTION_BAR_FONT_PACK: PackDefinition = PackDefinition("action_bar_font") {
        description = Component.literal("Shifts text on the action bar")
        include(path("packs/ActionBarFont"))
        for (i in 1..128) {
            addFont(BuiltInFonts.shiftedDownFont(i), BuiltInFontDefinitions.createDefaultFont(i))
        }
    }

    public val MINI_ACTION_BAR_FONT_PACK: PackDefinition = PackDefinition("mini_action_bar_font") {
        description = Component.literal("Shifts mini text on the action bar")
        include(path("packs/MiniActionBarFont"))
        for (i in 1..128) {
            addFont(BuiltInFonts.miniShiftedDownFont(i), BuiltInFontDefinitions.createMiniFont(i))
        }
    }

    public val SPACING_FONT_PACK: PackDefinition = PackDefinition("spacing_font") {
        description = Component.literal("Provides spacing utilities for text")
        include(path("packs/SpacingFont"))
        addFont(SpacingFontResources)
    }

    public val PADDING_FONT_PACK: PackDefinition = PackDefinition("padding_font") {
        description = Component.literal("Provides padding utilities for text")
        include(path("packs/PaddingFont"))
        addFont(PaddingSplitFontResources)
        addFont(PaddingNoSplitFontResources)
    }

    public val PIXEL_FONT_PACK: PackDefinition = PackDefinition("pixel_font") {
        description = Component.literal("Utilities for rendering pixel art")
        include(path("packs/PixelFont"))
        addFont(PixelFontResources)
    }

    public val HIDE_PLAYER_LIST_HEADS_PACK: PackDefinition = PackDefinition("hide_player_list_heads") {
        description = Component.literal("Utilities for hiding player list heads")
        include(path("packs/HidePlayerListHeads"))
    }

    public val HIDE_PLAYER_LIST_PING_PACK: PackDefinition = PackDefinition("hide_player_list_ping") {
        description = Component.literal("Utilities for hiding player list ping")
        include(path("packs/HidePlayerListPing"))
    }

    public val MINI_MINECRAFT_FONT_PACK: PackDefinition = PackDefinition("mini_minecraft") {
        description = Component.literal("Mini Minecraft style font")
        include(path("packs/MiniMinecraftFont"))
        addFont(BuiltInFonts.MINI_FONT, BuiltInFontDefinitions.createMiniFont())
    }

    public val BOUNDARY_SHADER_PACK: PackDefinition = PackDefinition("boundary_shader") {
        description = Component.literal("Shaders for rendering custom boundaries")
        include(path("packs/BoundaryShader"))
    }

    /**
     * All the langs bundled in a resource pack.
     */
    public val ARCADE_LANG_PACK: PackDefinition = PackDefinition("arcade_lang_pack") {
        description = Component.literal("Translations for arcade")
        setIcon(path("assets/icon.png"))
        addLangs("arcade-commands", false)
        addLangs("arcade-minigames", false)
        addLangs("arcade-virtual-visuals", false)
    }

    public fun createCustomGlowColorPack(replacer: ShaderUtils.ColorReplacer.() -> Unit): PackDefinition {
        return PackDefinition("custom_glow_colors") {
            description = Component.literal("Custom team glowing colors")
            addOutlineColors(replacer)
        }
    }

    public fun path(file: String): Path {
        return this.container.findPath(file).get()
    }
}
