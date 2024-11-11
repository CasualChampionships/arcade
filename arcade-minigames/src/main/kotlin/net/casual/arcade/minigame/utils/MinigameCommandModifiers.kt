package net.casual.arcade.minigame.utils

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.PlayerUtils.grantAdvancement
import net.casual.arcade.utils.PlayerUtils.revokeAdvancement
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.crafting.RecipeHolder

public enum class AdvancementModifier {
    Grant {
        override fun modifySingle(minigame: Minigame, player: ServerPlayer, advancement: AdvancementHolder): Component {
            player.grantAdvancement(advancement)
            return Component.translatable(
                "minigame.command.advancement.grant.single", advancement.id.toString(), player.scoreboardName
            )
        }

        override fun modifyAll(minigame: Minigame, player: ServerPlayer): Component {
            for (advancement in minigame.advancements.all()) {
                player.grantAdvancement(advancement)
            }
            return Component.translatable("minigame.command.advancement.grant.all", player.scoreboardName)
        }
    },
    Revoke {
        override fun modifySingle(minigame: Minigame, player: ServerPlayer, advancement: AdvancementHolder): Component {
            player.revokeAdvancement(advancement)
            return Component.translatable(
                "minigame.command.advancement.revoke.single", advancement.id.toString(), player.scoreboardName
            )
        }

        override fun modifyAll(minigame: Minigame, player: ServerPlayer): Component {
            for (advancement in minigame.advancements.all()) {
                player.revokeAdvancement(advancement)
            }
            return Component.translatable("minigame.command.advancement.revoke.all", player.scoreboardName)
        }
    };

    public abstract fun modifySingle(minigame: Minigame, player: ServerPlayer, advancement: AdvancementHolder): Component

    public abstract fun modifyAll(minigame: Minigame, player: ServerPlayer): Component
}

public enum class RecipeModifier {
    Grant {
        override fun modifySingle(minigame: Minigame, player: ServerPlayer, recipe: RecipeHolder<*>): Component {
            minigame.recipes.grantAll(player, listOf(recipe))
            return Component.translatable(
                "minigame.command.recipe.grant.single", recipe.id.location().toString(), player.scoreboardName
            )
        }

        override fun modifyAll(minigame: Minigame, player: ServerPlayer): Component {
            minigame.recipes.grantAll(player, minigame.recipes.all())
            return Component.translatable("minigame.command.recipe.grant.all", player.scoreboardName)
        }
    },
    Revoke {
        override fun modifySingle(minigame: Minigame, player: ServerPlayer, recipe: RecipeHolder<*>): Component {
            minigame.recipes.revokeAll(player, listOf(recipe))
            return Component.translatable(
                "minigame.command.recipe.revoke.single", recipe.id.location().toString(), player.scoreboardName
            )
        }

        override fun modifyAll(minigame: Minigame, player: ServerPlayer): Component {
            minigame.recipes.revokeAll(player, minigame.recipes.all())
            return Component.translatable("minigame.command.recipe.revoke.all", player.scoreboardName)
        }
    };

    public abstract fun modifySingle(minigame: Minigame, player: ServerPlayer, recipe: RecipeHolder<*>): Component

    public abstract fun modifyAll(minigame: Minigame, player: ServerPlayer): Component
}