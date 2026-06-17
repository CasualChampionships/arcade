package net.casual.arcade.test.guis

import net.casual.arcade.guis.core.Gui
import net.casual.arcade.guis.core.container.ContainerGui
import net.casual.arcade.guis.core.container.GuiItem
import net.casual.arcade.guis.utils.ContainerType
import net.casual.arcade.guis.utils.SlotClickAction
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.player.username
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TestContainerGui(player: ServerPlayer, overrideInventory: Boolean = false): ContainerGui(player, ContainerType.Dropper, overrideInventory) {
    private var overrideChildInventory = false
    private var valid = true

    init {
        this.setTitleToNext()
    }

    override fun click(slot: Int, action: SlotClickAction) {
        logger.info("${this.player.username} clicked $slot with action $action")
        super.click(slot, action)
    }

    override fun valid(): Boolean {
        return this.valid
    }

    override fun onOpen() {
        logger.info("${this.player.username} opened the gui")

        this.setSlot(0, FlipFloppingItem)
        this.setSlot(1, Items.WOOL.red.named("Close")) { this.close() }
        this.setSlot(2, Items.WOOL.blue.named("Open Child")) {
            val child = TestContainerGui(this.player, this.overrideChildInventory)
            child.setParent(this)
            child.open()
        }
        this.setSlot(3, Items.WOOL.green.named("Open Parent Or Close")) { this.openParentOrClose() }
        this.setSlot(4, Items.WOOL.purple.named("Mark Invalid")) { this.valid = false }
        this.setSlot(5, Items.WOOL.yellow.named("Next Title")) { this.setTitleToNext() }
        this.setSlot(6, Items.WOOL.lightGray.named("Disable Spectator Clicking")) { this.canSpectatorsClick = false }
        this.setSlot(7, Items.WOOL.pink.named("Override Child Inventory")) { this.overrideChildInventory = true }
    }

    override fun onClose(reason: Gui.CloseReason) {
        logger.info("${this.player.username} closed the gui due to $reason")
    }

    private fun setTitleToNext() {
        this.setTitle(Component.literal("Gui ${counter++}"))
    }

    private object FlipFloppingItem: GuiItem {
        private var ticks = 0

        override fun tick() {
            this.ticks++
        }

        override fun display(): ItemStack {
            if ((this.ticks / 20) % 2 == 0) {
//                return ItemStack(Items.DIRT)
            }
            return ItemStack(Items.GRASS_BLOCK)
        }
    }

    companion object {
        private var counter = 0

        val logger: Logger = LoggerFactory.getLogger("GuiEventTest")
    }
}