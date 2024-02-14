package net.casual.arcade.resources

import eu.pb4.polymer.resourcepack.api.ResourcePackCreator
import net.casual.arcade.Arcade

/**
 * Contains some commonly used resource packs.
 */
public object ArcadePacks {
    public val ACTION_BAR_FONT_CREATOR: ResourcePackCreator = ResourcePackCreator.create()
    public val SPACES_PACK_CREATOR: ResourcePackCreator = ResourcePackCreator.create()
    public val NO_SHADOW_CREATOR: ResourcePackCreator = ResourcePackCreator.create()

    init {
        ACTION_BAR_FONT_CREATOR.addAssetSource(Arcade.container.findPath("packs/ActionBarFont/assets").get())
        SPACES_PACK_CREATOR.addAssetSource(Arcade.container.findPath("packs/Spaces/assets").get())
        NO_SHADOW_CREATOR.addAssetSource(Arcade.container.findPath("packs/NoShadow/assets").get())
    }
}