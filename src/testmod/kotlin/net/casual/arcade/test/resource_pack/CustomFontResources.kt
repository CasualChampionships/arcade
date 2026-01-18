package net.casual.arcade.test.resource_pack

import net.casual.arcade.Arcade
import net.casual.arcade.resources.font.IndexedFontResources
import net.casual.arcade.utils.arcade
import javax.imageio.ImageIO
import kotlin.io.path.inputStream

object CustomFontResources: IndexedFontResources(arcade("testing")) {
    val LOGO = bitmap(at("logo")) {
        Arcade.container.findPath("assets/icon.png").get().inputStream().use(ImageIO::read)
    }
}