package net.casual.arcade.resources

import net.casual.arcade.Arcade
import net.casual.arcade.font.heads.PlayerHeadFont
import net.casual.arcade.font.padding.PaddingNoSplitFont
import net.casual.arcade.font.padding.PaddingSplitFont
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ResourcePackUtils.addBitmapFont
import java.nio.file.Path
import kotlin.io.path.readBytes

/**
 * Contains some commonly used resource packs.
 */
@Suppress("MemberVisibilityCanBePrivate")
public object ArcadePacks {
    public val ACTION_BAR_FONT_PACK: NamedResourcePackCreator
    public val SPACES_FONT_PACK: NamedResourcePackCreator
    public val PADDING_FONT_PACK: NamedResourcePackCreator
    public val NO_SHADOW_PACK: NamedResourcePackCreator
    public val PLAYER_HEADS_PACK: NamedResourcePackCreator
    public val HIDE_PLAYER_LIST_HEADS_PACK: NamedResourcePackCreator
    public val HIDE_PLAYER_LIST_PING_PACK: NamedResourcePackCreator
    public val MINI_MINECRAFT_FONT: NamedResourcePackCreator

    init {
        ACTION_BAR_FONT_PACK = NamedResourcePackCreator.named("action_bar_font") {
            addAssetSource(path("packs/ActionBarFont"))
            packDescription = "Shifts text on the action bar".literal()
        }
        SPACES_FONT_PACK = NamedResourcePackCreator.named("spaces_font") {
            addAssetSource(path("packs/SpacesFont"))
            packDescription = "Provides spacing utilities for text".literal()
        }
        PADDING_FONT_PACK = NamedResourcePackCreator.named("padding_font") {
            addAssetSource(path("packs/PaddingFont"))
            addBitmapFont(PaddingSplitFont)
            addBitmapFont(PaddingNoSplitFont)
            packDescription = "Provides padding utilities for text".literal()
        }
        NO_SHADOW_PACK = NamedResourcePackCreator.named("no_shadow") {
            addAssetSource(path("packs/NoShadow"))
            packDescription = "Utilities for removing text shadows".literal()
        }
        PLAYER_HEADS_PACK = NamedResourcePackCreator.named("player_heads") {
            val location = path("packs/PlayerHeads")
            addAssetSource(location)
            addBitmapFont(PlayerHeadFont)
            packIcon = location.resolve("assets/arcade/textures/font/steve.png").readBytes()
            packDescription = "Utilities for rendering player heads".literal()
        }
        HIDE_PLAYER_LIST_HEADS_PACK = NamedResourcePackCreator.named("hide_player_list_heads") {
            addAssetSource(path("packs/HidePlayerListHeads"))
            packDescription = "Utilities for hiding player list heads".literal()
        }
        HIDE_PLAYER_LIST_PING_PACK = NamedResourcePackCreator.named("hide_player_list_ping") {
            addAssetSource(path("packs/HidePlayerListPing"))
            packDescription = "Utilities for hiding player list ping".literal()
        }
        MINI_MINECRAFT_FONT = NamedResourcePackCreator.named("mini_minecraft") {
            addAssetSource(path("packs/MiniMinecraftFont"))
            packDescription = "Mini Minecraft style font".literal()
        }
    }

    private fun path(file: String): Path {
        return Arcade.container.findPath(file).get()
    }
}