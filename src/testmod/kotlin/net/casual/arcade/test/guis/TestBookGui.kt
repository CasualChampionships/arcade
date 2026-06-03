package net.casual.arcade.test.guis

import net.casual.arcade.guis.core.book.BookGui
import net.casual.arcade.guis.utils.BookClickAction
import net.casual.arcade.guis.utils.addBookPage
import net.casual.arcade.guis.utils.setBookAuthor
import net.casual.arcade.guis.utils.setBookTitle
import net.casual.arcade.utils.component.bold
import net.casual.arcade.utils.component.click
import net.casual.arcade.utils.component.crimson
import net.casual.arcade.utils.component.gold
import net.casual.arcade.utils.component.red
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class TestBookGui(player: ServerPlayer): BookGui(player) {
    private var anger = 0

    init {
        this.book = ItemStack(Items.WRITTEN_BOOK)
            .setBookAuthor("Harper Lee")
            .setBookTitle("To Kill a Mockingbird")
            .addBookPage(
                Component.literal("Chapter 1").bold(),
                Component.literal(
                    "When he was nearly thirteen, my brother Jem got his arm badly broken at the elbow. " +
                        "When it healed, and Jem's fears of never being able to play football were assuaged, " +
                        "he was seldom self-conscious about his injury."
                )
            )
            .addBookPage(
                Component.literal("[Click to jump to page 5]").gold().click(ClickEvent.ChangePage(5))
            )
            .addBookPage()
            .addBookPage()
            .addBookPage(
                Component.literal("Surprise!")
            )
    }

    override fun click(action: BookClickAction) {
        if (action == BookClickAction.TakeBook) {
            this.getAngryAtYouForTryingToTakeTheBook()
            return
        }

        super.click(action)
    }

    private fun getAngryAtYouForTryingToTakeTheBook() {
        val anger = this.anger++
        val message = ANGRY_MESSAGES.getOrNull(anger)
        if (message == null) {
            this.player.kill(this.player.level())
            return
        }

        this.book.addBookPage(message)
        this.setPage(5 + anger)
    }

    private companion object {
        val ANGRY_MESSAGES = arrayOf(
            Component.literal("Please don't do that!"),
            Component.literal("Hey, I asked nicely, don't do it.").crimson(),
            Component.literal("You're asking for it!").red(),
            Component.literal("GRRRRR!!!!").red().bold()
        )
    }
}