package net.casual.arcade.resources

import net.casual.arcade.Arcade

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
        }
        SPACES_FONT_PACK = NamedResourcePackCreator.named("spaces_font") {
            addAssetSource(Arcade.container.findPath("packs/SpacesFont").get())
        }
        NO_SHADOW_PACK = NamedResourcePackCreator.named("no_shadow") {
            addAssetSource(Arcade.container.findPath("packs/NoShadow").get())
        }
    }
}