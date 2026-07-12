package net.casual.arcade.test.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.executes
import net.casual.arcade.commands.literal
import net.casual.arcade.nametags.Nametag
import net.casual.arcade.nametags.extensions.EntityNametagExtension.Companion.nametagExtension
import net.casual.arcade.virtual.entity.observer.Observer
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.ComponentArgument
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity

@Suppress("unused")
object NametagCommand: CommandTree<CommandSourceStack> {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("nametag") {
            literal("add") {
                literal("simple") {
                    argument("targets", EntityArgument.entities()) {
                        argument("text", ComponentArgument.textComponent(buildContext)) {
                            executes(::addNametag)
                        }
                    }
                }
                literal("blinking") {
                    argument("targets", EntityArgument.entities()) {
                        executes(::addBlinkingNametag)
                    }
                }
            }
        }
    }

    private fun addNametag(context: CommandContext<CommandSourceStack>) {
        val targets = EntityArgument.getEntities(context, "targets")
        for (entity in targets) {
            val text = ComponentArgument.getResolvedComponent(context, "text", entity)
            entity.nametagExtension.add(Nametag.simple(text))
        }
    }

    private fun addBlinkingNametag(context: CommandContext<CommandSourceStack>) {
        val targets = EntityArgument.getEntities(context, "targets")
        val text = Component.literal("Blinky")
        for (entity in targets) {
            entity.nametagExtension.add(object: Nametag {
                override fun getComponent(observee: Entity): Component {
                    return text
                }

                override fun isObservable(observee: Entity, observer: Observer): Boolean {
                    return (observee.level().gameTime / 20) % 2 == 0L
                }
            })
        }
    }
}