package net.casual.arcade.resources

import net.casual.arcade.Arcade
import net.casual.arcade.utils.ComponentUtils.literal

/**
 * Contains some commonly used resource packs.
 */
@Suppress("JoinDeclarationAndAssignment", "MemberVisibilityCanBePrivate")
public object ArcadePacks {
    public val ACTION_BAR_FONT_PACK: NamedResourcePackCreator
    public val SPACES_FONT_PACK: NamedResourcePackCreator
    public val NO_SHADOW_PACK: NamedResourcePackCreator

    init {
        ACTION_BAR_FONT_PACK = NamedResourcePackCreator.named("action_bar_font") {
            addAssetSource(Arcade.container.findPath("packs/ActionBarFont").get())
            packDescription = "Resource pack which provides fonts to shift text on the action bar".literal()
        }
        SPACES_FONT_PACK = NamedResourcePackCreator.named("spaces_font") {
            addAssetSource(Arcade.container.findPath("packs/SpacesFont").get())
            packDescription = "Resource pack which provides spacing utilities for text".literal()
        }
        NO_SHADOW_PACK = NamedResourcePackCreator.named("no_shadow") {
            addAssetSource(Arcade.container.findPath("packs/NoShadow").get())
            packDescription = "Resource pack which provides utilities for removing text shadows".literal()
        }
    }
}