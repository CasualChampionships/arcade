package net.casual.arcade.resources

import net.casual.arcade.resources.creator.NamedResourcePackCreator
import net.casual.arcade.rp.font.heads.PlayerHeadFont
import net.casual.arcade.resources.font.padding.PaddingNoSplitFontResources
import net.casual.arcade.resources.font.padding.PaddingSplitFontResources
import net.casual.arcade.resources.utils.ResourcePackUtils
import net.casual.arcade.resources.utils.ResourcePackUtils.addFont
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.ComponentUtils.literal
import net.fabricmc.api.ModInitializer
import java.nio.file.Path
import kotlin.io.path.readBytes

/**
 * Contains some commonly used resource packs.
 */
public object ArcadeResourcePacks: ModInitializer {
    public val ACTION_BAR_FONT_PACK: NamedResourcePackCreator by lazy {
        NamedResourcePackCreator.named("action_bar_font") {
            addAssetSource(path("packs/ActionBarFont"))
            packDescription = "Shifts text on the action bar".literal()
        }
    }

    public val MINI_ACTION_BAR_FONT_PACK: NamedResourcePackCreator by lazy {
        NamedResourcePackCreator.named("mini_action_bar_font") {
            addAssetSource(path("packs/MiniActionBarFont"))
            packDescription = "Shifts mini text on the action bar".literal()
        }
    }

    public val SPACES_FONT_PACK: NamedResourcePackCreator by lazy {
        NamedResourcePackCreator.named("spaces_font") {
            addAssetSource(path("packs/SpacesFont"))
            packDescription = "Provides spacing utilities for text".literal()
        }
    }

    public val PADDING_FONT_PACK: NamedResourcePackCreator by lazy {
        NamedResourcePackCreator.named("padding_font") {
            addAssetSource(path("packs/PaddingFont"))
            addFont(PaddingSplitFontResources)
            addFont(PaddingNoSplitFontResources)
            packDescription = "Provides padding utilities for text".literal()
        }
    }

    public val NO_SHADOW_PACK: NamedResourcePackCreator by lazy {
        NamedResourcePackCreator.named("no_shadow") {
            addAssetSource(path("packs/NoShadow"))
            packDescription = "Utilities for removing text shadows".literal()
        }
    }

    public val PLAYER_HEADS_PACK: NamedResourcePackCreator by lazy {
        NamedResourcePackCreator.named("player_heads") {
            val location = path("packs/PlayerHeads")
            addAssetSource(location)
            addFont(PlayerHeadFont)
            packIcon = location.resolve("assets/arcade/textures/font/steve.png").readBytes()
            packDescription = "Utilities for rendering player heads".literal()
        }
    }

    public val HIDE_PLAYER_LIST_HEADS_PACK: NamedResourcePackCreator by lazy {
        NamedResourcePackCreator.named("hide_player_list_heads") {
            addAssetSource(path("packs/HidePlayerListHeads"))
            packDescription = "Utilities for hiding player list heads".literal()
        }
    }

    public val HIDE_PLAYER_LIST_PING_PACK: NamedResourcePackCreator by lazy {
        NamedResourcePackCreator.named("hide_player_list_ping") {
            addAssetSource(path("packs/HidePlayerListPing"))
            packDescription = "Utilities for hiding player list ping".literal()
        }
    }

    public val MINI_MINECRAFT_FONT: NamedResourcePackCreator by lazy {
        NamedResourcePackCreator.named("mini_minecraft") {
            addAssetSource(path("packs/MiniMinecraftFont"))
            packDescription = "Mini Minecraft style font".literal()
        }
    }

    override fun onInitialize() {
        ResourcePackUtils.registerEvents()
    }

    private fun path(file: String): Path {
        return ArcadeUtils.container.findPath(file).get()
    }
}