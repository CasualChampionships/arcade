package net.casual.arcade.minigame.utils

import net.casual.arcade.utils.PlayerUtils.grantAdvancement
import net.casual.arcade.utils.PlayerUtils.revokeAdvancement
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

public enum class AdvancementModifier {
    Grant {
        override fun modify(player: ServerPlayer, advancement: AdvancementHolder) {
            player.grantAdvancement(advancement)
        }

        override fun singleSuccessMessage(player: ServerPlayer, advancement: AdvancementHolder): Component {
            return Component.translatable("minigame.command.advancement.grant.single", advancement.id.toString(), player.scoreboardName)
        }

        override fun allSuccessMessage(player: ServerPlayer): Component {
            return Component.translatable("minigame.command.advancement.grant.all", player.scoreboardName)
        }
    },
    Revoke {
        override fun modify(player: ServerPlayer, advancement: AdvancementHolder) {
            player.revokeAdvancement(advancement)
        }

        override fun singleSuccessMessage(player: ServerPlayer, advancement: AdvancementHolder): Component {
            return Component.translatable("minigame.command.advancement.revoke.single", advancement.id.toString(), player.scoreboardName)
        }

        override fun allSuccessMessage(player: ServerPlayer): Component {
            return Component.translatable("minigame.command.advancement.revoke.all", player.scoreboardName)
        }
    };

    public abstract fun modify(player: ServerPlayer, advancement: AdvancementHolder)

    public abstract fun singleSuccessMessage(player: ServerPlayer, advancement: AdvancementHolder): Component

    public abstract fun allSuccessMessage(player: ServerPlayer): Component
}