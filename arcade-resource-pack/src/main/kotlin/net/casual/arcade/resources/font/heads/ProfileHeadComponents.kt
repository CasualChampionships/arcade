/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.font.heads

import net.casual.arcade.utils.DynamicResolvableProfile
import net.casual.arcade.utils.StaticResolvableProfile
import net.casual.arcade.utils.component.font
import net.minecraft.core.ClientAsset
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FontDescription
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.PlayerSkin
import net.minecraft.world.item.component.ResolvableProfile
import java.util.*

public object ProfileHeadComponents: TexturedHeadComponents {
    private const val SQUARE = "█"
    private val DEFAULT = this.createDefaultHead()

    override fun getDefault(): Component {
        return DEFAULT
    }

    override suspend fun getHeadFor(resolvable: ResolvableProfile): Component {
        return Component.literal(SQUARE).font(FontDescription.PlayerSprite(resolvable, true))
    }

    public fun getHeadNowFor(resolvable: ResolvableProfile): Component {
        return Component.literal(SQUARE).font(FontDescription.PlayerSprite(resolvable, true))
    }

    public fun getHeadNowFor(player: ServerPlayer): Component {
        return this.getHeadNowFor(StaticResolvableProfile(player.gameProfile))
    }

    public fun getHeadNowFor(username: String): Component {
        return this.getHeadNowFor(DynamicResolvableProfile(username))
    }

    public fun getHeadNowFor(uuid: UUID): Component {
        return this.getHeadNowFor(DynamicResolvableProfile(uuid))
    }

    private fun createDefaultHead(): Component {
        val profile = StaticResolvableProfile(skin = this.createSteveSkinPatch())
        val font = FontDescription.PlayerSprite(profile, true)
        return Component.literal(SQUARE).font(font)
    }

    private fun createSteveSkinPatch(): PlayerSkin.Patch {
        val steve = ResourceLocation.withDefaultNamespace("entity/player/wide/steve")
        return PlayerSkin.Patch(
            Optional.of(ClientAsset.ResourceTexture(steve)),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        )
    }
}