package net.casual.arcade.test.guis.dialog

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.guis.utils.dialog.*
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.player.username
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object TestDialog {
    private val REGISTRY = DialogCustomActionRegistry()

    private val CONFIRMATION_ID = REGISTRY.register(
        arcade("dialog_confirmation_test"), TestData.CODEC, ::onSubmitTestDataSuccess
    )

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
        DialogCustomActionRegistry.register(REGISTRY)
    }

    private fun onSubmitTestDataSuccess(player: ServerPlayer, data: TestData) {
        ArcadeUtils.logger.info("${player.username} submitted valid test data: $data")
    }

    private data class TestData(val toggle: Boolean, val range: Float) {
        companion object {
            val CODEC: Codec<TestData> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.BOOL.fieldOf("toggle").forGetter(TestData::toggle),
                    Codec.FLOAT.fieldOf("range").forGetter(TestData::range)
                ).apply(instance, ::TestData)
            }
        }
    }
}