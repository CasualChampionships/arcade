package net.casual.arcade.resources

import net.casual.arcade.resources.creator.NamedResourcePackCreator
import net.casual.arcade.resources.font.heads.PlayerHeadFont
import net.casual.arcade.resources.font.padding.PaddingNoSplitFontResources
import net.casual.arcade.resources.font.padding.PaddingSplitFontResources
import net.casual.arcade.resources.font.spacing.SpacingFontResources
import net.casual.arcade.resources.utils.FontUtils
import net.casual.arcade.resources.utils.ResourcePackUtils
import net.casual.arcade.resources.utils.ResourcePackUtils.addCustomOutlineColors
import net.casual.arcade.resources.utils.ResourcePackUtils.addFont
import net.casual.arcade.resources.utils.ResourcePackUtils.addLangsFromData
import net.casual.arcade.resources.utils.ShaderUtils
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ResourceUtils
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component
import java.nio.file.Path
import kotlin.io.path.readBytes

/**
 * Contains some commonly used resource packs.
 */
public object ArcadeResourcePacks: ModInitializer {
    private val container = FabricLoader.getInstance().getModContainer("arcade-resource-pack").get()

    public val ACTION_BAR_FONT_PACK: NamedResourcePackCreator by lazy {
        NamedResourcePackCreator.named("action_bar_font") {
            addAssetSource(path("packs/ActionBarFont"))
            for (i in 1..64) {
                addFont(ResourceUtils.arcade("default_shifted_down_$i")) { FontUtils.createDefaultFont(i) }
            }
            packDescription = Component.literal("Shifts text on the action bar")
        }
    }

    public val MINI_ACTION_BAR_FONT_PACK: NamedResourcePackCreator by lazy {
        NamedResourcePackCreator.named("mini_action_bar_font") {
            addAssetSource(path("packs/MiniActionBarFont"))
            for (i in 1..64) {
                addFont(ResourceUtils.arcade("mini_shifted_down_$i")) { FontUtils.createMiniFont(i) }
            }
            packDescription = Component.literal("Shifts mini text on the action bar")
        }
    }

    public val SPACING_FONT_PACK: NamedResourcePackCreator by lazy {
        NamedResourcePackCreator.named("spacing_font") {
            addFont(SpacingFontResources)
            addAssetSource(path("packs/SpacingFont"))
            packDescription = Component.literal("Provides spacing utilities for text")
        }
    }

    public val PADDING_FONT_PACK: NamedResourcePackCreator by lazy {
        NamedResourcePackCreator.named("padding_font") {
            addAssetSource(path("packs/PaddingFont"))
            addFont(PaddingSplitFontResources)
            addFont(PaddingNoSplitFontResources)
            packDescription = Component.literal("Provides padding utilities for text")
        }
    }

    public val PLAYER_HEADS_PACK: NamedResourcePackCreator by lazy {
        NamedResourcePackCreator.named("player_heads") {
            val location = path("packs/PlayerHeads")
            addAssetSource(location)
            addFont(PlayerHeadFont)
            packIcon = location.resolve("assets/arcade/textures/font/steve.png").readBytes()
            packDescription = Component.literal("Utilities for rendering player heads")
        }
    }

    public val HIDE_PLAYER_LIST_HEADS_PACK: NamedResourcePackCreator by lazy {
        NamedResourcePackCreator.named("hide_player_list_heads") {
            addAssetSource(path("packs/HidePlayerListHeads"))
            packDescription = Component.literal("Utilities for hiding player list heads")
        }
    }

    public val HIDE_PLAYER_LIST_PING_PACK: NamedResourcePackCreator by lazy {
        NamedResourcePackCreator.named("hide_player_list_ping") {
            addAssetSource(path("packs/HidePlayerListPing"))
            packDescription = Component.literal("Utilities for hiding player list ping")
        }
    }

    public val MINI_MINECRAFT_FONT: NamedResourcePackCreator by lazy {
        NamedResourcePackCreator.named("mini_minecraft") {
            addAssetSource(path("packs/MiniMinecraftFont"))
            addFont(ComponentUtils.MINI_FONT, FontUtils::createMiniFont)
            packDescription = Component.literal("Mini Minecraft style font")
        }
    }

    /**
     * All the langs bundled in a resource pack.
     */
    public val ARCADE_LANG_PACK: NamedResourcePackCreator by lazy {
        NamedResourcePackCreator.named("arcade_lang_pack") {
            addLangsFromData("arcade-commands")
            addLangsFromData("arcade-minigames")
            packIcon = path("assets/icon.png").readBytes()
            packDescription = Component.literal("Translations for arcade")
        }
    }

    override fun onInitialize() {
        ResourcePackUtils.registerEvents()
    }

    public fun createCustomGlowColorPack(replacer: ShaderUtils.ColorReplacer.() -> Unit): NamedResourcePackCreator {
        return NamedResourcePackCreator.named("custom_glow_colors") {
            addCustomOutlineColors(replacer)
            packDescription = Component.literal("Custom team glowing colors")
        }
    }

    private fun path(file: String): Path {
        return this.container.findPath(file).get()
    }
}