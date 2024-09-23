package net.casual.arcade.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack

public interface CommandTree {
    public fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack>

    public companion object {
        public inline fun <S> buildLiteral(
            name: String,
            builder: LiteralArgumentBuilder<S>.() -> Unit = { }
        ): LiteralArgumentBuilder<S> {
            val root = LiteralArgumentBuilder.literal<S>(name)
            root.builder()
            return root
        }

        public inline fun <S> createLiteral(
            name: String,
            builder: LiteralArgumentBuilder<S>.() -> Unit = { }
        ): LiteralCommandNode<S> {
            return this.buildLiteral(name, builder).build()
        }
    }
}