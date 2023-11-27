package net.casual.arcade.minigame

import net.casual.arcade.settings.display.DisplayableGameSettingBuilder.Companion.bool
import net.casual.arcade.settings.display.DisplayableSettings
import net.casual.arcade.utils.ItemUtils.hideTooltips
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.ScreenUtils
import net.casual.arcade.utils.SettingsUtils.defaultOptions
import net.minecraft.world.MenuProvider
import net.minecraft.world.item.Items

/**
 * This class is the base class for all minigame settings.
 *
 * This contains the default settings that are available
 * to every minigame.
 *
 * All registered settings can be modified using a UI in-game using the command:
 *
 * `/minigame settings <minigame-uuid>`
 *
 * Alternatively you can do it directly with commands:
 *
 * `/minigame settings <minigame-uuid> set <setting> from option <option>`
 *
 * `/minigame settings <minigame-uuid> set <setting> from value <value>`
 *
 */
public open class MinigameSettings: DisplayableSettings() {
    /**
     * Whether pvp is enabled for this minigame.
     */
    public var canPvp: Boolean by this.register(bool {
        name = "pvp"
        display = Items.IRON_SWORD.named("PvP").hideTooltips()
        value = true
        defaultOptions()
    })

    /**
     * Whether the player will lose hunger.
     */
    public var canGetHungry: Boolean by this.register(bool {
        name = "hunger"
        display = Items.COOKED_BEEF.named("Hunger")
        value = true
        defaultOptions()
    })

    /**
     * Whether players can take damage.
     */
    public var canTakeDamage: Boolean by this.register(bool {
        name = "can_take_damage"
        display = Items.SHIELD.named("Damage")
        value = true
        defaultOptions()
    })

    /**
     * Whether players can break blocks.
     */
    public var canBreakBlocks: Boolean by this.register(bool {
        name = "can_break_blocks"
        display = Items.DIAMOND_PICKAXE.named("Break Blocks")
        value = true
        defaultOptions()
    })

    /**
     * Whether players can place blocks.
     */
    public var canPlaceBlocks: Boolean by this.register(bool {
        name = "can_place_blocks"
        display = Items.DIRT.named("Place Blocks")
        value = true
        defaultOptions()
    })

    /**
     * Whether players can throw items in this minigame
     */
    public var canThrowItems: Boolean by this.register(bool {
        name = "can_throw_items"
        display = Items.ENDER_PEARL.named("Throw Items")
        value = true
        defaultOptions()
    })

    /**
     * Whether players can pick up items.
     */
    public var canPickupItems: Boolean by this.register(bool {
        name = "can_pickup_items"
        display = Items.COBBLESTONE.named("Pickup Items")
        value = true
        defaultOptions()
    })

    /**
     * Whether players can attack entities.
     */
    public var canAttackEntities: Boolean by this.register(bool {
        name = "can_attack_entities"
        display = Items.DIAMOND_AXE.named("Attack Entities").hideTooltips()
        value = true
        defaultOptions()
    })

    /**
     * Whether players can interact with entities.
     */
    public var canInteractEntities: Boolean by this.register(bool {
        name = "can_interact_entities"
        display = Items.VILLAGER_SPAWN_EGG.named("Interact With Entities")
        value = true
        defaultOptions()
        listener { _, value ->
            canInteractAllSetting.setQuietly(value && canInteractItems && canInteractBlocks)
        }
    })

    /**
     * Whether players can interact with blocks.
     */
    public var canInteractBlocks: Boolean by this.register(bool {
        name = "can_interact_blocks"
        display = Items.FURNACE.named("Interact With Blocks")
        value = true
        defaultOptions()
        listener { _, value ->
            canInteractAllSetting.setQuietly(value && canInteractItems && canInteractEntities)
        }
    })

    /**
     * Whether players can interact with items.
     */
    public var canInteractItems: Boolean by this.register(bool {
        name = "can_interact_items"
        display = Items.WRITTEN_BOOK.named("Interact With Items").hideTooltips()
        value = true
        defaultOptions()
        listener { _, value ->
            canInteractAllSetting.setQuietly(value && canInteractBlocks && canInteractEntities)
        }
    })

    private val canInteractAllSetting = this.register(bool {
        name = "can_interact_all"
        display = Items.OBSERVER.named("Interact With Everything")
        value = true
        defaultOptions()
        listener { _, value ->
            canInteractBlocks = value
            canInteractEntities = value
            canInteractItems = value
        }
    })

    /**
     * Whether players can interact with anything.
     */
    public var canInteractAll: Boolean by this.canInteractAllSetting

    /**
     * This creates a menu which can be displayed to a
     * player to directly interact with the settings.
     *
     * @return The menu provider.
     */
    override fun menu(): MenuProvider {
        return ScreenUtils.createSettingsMenu(this, ScreenUtils.DefaultMinigameSettingsComponent)
    }
}