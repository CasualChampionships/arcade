package net.casual.arcade.test.guis.dialog

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.player.PlayerCustomClickActionEvent
import net.casual.arcade.guis.utils.dialog.ConfirmationDialog
import net.casual.arcade.guis.utils.dialog.CustomAll
import net.casual.arcade.guis.utils.dialog.DialogListDialog
import net.casual.arcade.guis.utils.dialog.StaticAction
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.player.username
import net.minecraft.network.chat.Component

object TestDialog {
    private val CONFIRMATION_ID = arcade("dialog_confirmation_test")

    fun create(): DialogListDialog {
        return DialogListDialog {
            title = Component.literal("Test Dialog Main Menu")
            addPlainMessageBody(Component.literal("Hello world!"))

            dialog(ConfirmationDialog {
                addBooleanInput("toggle") { }
                addNumberRangeInput("range") { }
                yesButton {
                    label = Component.literal("I Agree")
                    action = CustomAll { id = CONFIRMATION_ID }
                }
                noButton {
                    label = Component.literal("I Disagree")
                }
            })

            exitAction {
                label = Component.literal("Exit Action")
                tooltip = Component.literal("Exit Action Tooltip")
                action = StaticAction {
                    runCommand("/me I just exited the test dialog")
                }
            }
        }
    }

    internal fun registerEvents() {
        GlobalEventHandler.Server.register<PlayerCustomClickActionEvent>(::onPlayerCustomClickAction)
    }

    private fun onPlayerCustomClickAction(event: PlayerCustomClickActionEvent) {
        if (!event.consumed()) {
            val (player, id, payload) = event
            if (id == CONFIRMATION_ID) {
                ArcadeUtils.logger.info("${player.username} submitted: $payload")
            }
        }
    }
}