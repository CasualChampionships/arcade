package net.casual.arcade.minigame

import net.casual.arcade.settings.GameSetting
import net.casual.arcade.settings.display.DisplayableGameSettingBuilder.Companion.bool
import net.casual.arcade.settings.display.DisplayableSettings
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ItemUtils.hideTooltips
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.ItemUtils.setLore
import net.casual.arcade.utils.ScreenUtils
import net.casual.arcade.utils.SettingsUtils.defaultOptions
import net.minecraft.server.level.ServerPlayer
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
public open class MinigameSettings(private val minigame: Minigame<*>): DisplayableSettings() {
    /**
     * Whether pvp is enabled for this minigame.
     */
    @JvmField
    public val canPvp: GameSetting<Boolean> = this.register(bool {
        name = "pvp"
        display = Items.IRON_SWORD.named("PvP").hideTooltips().setLore(
            "If enabled this will allow players to pvp with one another.".literal()
        )
        value = true
        override = isAdminOverride(minigame, true)
        defaultOptions()
    })

    /**
     * Whether the player will lose hunger.
     */
    @JvmField
    public val canGetHungry: GameSetting<Boolean> = this.register(bool {
        name = "hunger"
        display = Items.COOKED_BEEF.named("Hunger").setLore(
            "If enabled this will cause players to lose hunger over time.".literal()
        )
        value = true
        defaultOptions()
    })

    /**
     * Whether players can take damage.
     */
    @JvmField
    public val canTakeDamage: GameSetting<Boolean> = this.register(bool {
        name = "can_take_damage"
        display = Items.SHIELD.named("Damage").setLore(
            "If enabled players will be able to take damage.".literal()
        )
        value = true
        defaultOptions()
    })

    /**
     * Whether players can break blocks.
     */
    @JvmField
    public val canBreakBlocks: GameSetting<Boolean> = this.register(bool {
        name = "can_break_blocks"
        display = Items.DIAMOND_PICKAXE.named("Break Blocks").setLore(
            "If enabled players will be able to break blocks.".literal()
        )
        value = true
        override = isAdminOverride(minigame, true)
        defaultOptions()
    })

    /**
     * Whether players can place blocks.
     */
    @JvmField
    public val canPlaceBlocks: GameSetting<Boolean> = this.register(bool {
        name = "can_place_blocks"
        display = Items.DIRT.named("Place Blocks").setLore(
            "If enabled players will be able to place blocks.".literal()
        )
        value = true
        override = isAdminOverride(minigame, true)
        defaultOptions()
    })

    /**
     * Whether players can drop items in this minigame
     */
    @JvmField
    public val canDropItems: GameSetting<Boolean> = this.register(bool {
        name = "can_drop_items"
        display = Items.DIORITE.named("Drop Items").setLore(
            "If enabled players will be able to drop items".literal()
        )
        value = true
        override = isAdminOverride(minigame, true)
        defaultOptions()
    })

    /**
     * Whether players can pick up items.
     */
    @JvmField
    public val canPickupItems: GameSetting<Boolean> = this.register(bool {
        name = "can_pickup_items"
        display = Items.COBBLESTONE.named("Pickup Items").setLore(
            "If enabled players will be able to pick up items.".literal()
        )
        value = true
        override = isAdminOverride(minigame, true)
        defaultOptions()
    })

    /**
     * Whether players can attack entities.
     */
    @JvmField
    public val canAttackEntities: GameSetting<Boolean> = this.register(bool {
        name = "can_attack_entities"
        display = Items.DIAMOND_AXE.named("Attack Entities").hideTooltips().setLore(
            "If enabled players will be able to attack all other entities.".literal()
        )
        value = true
        override = isAdminOverride(minigame, true)
        defaultOptions()
    })

    /**
     * Whether players can interact with entities.
     */
    @JvmField
    public val canInteractEntities: GameSetting<Boolean> = this.register(bool {
        name = "can_interact_entities"
        display = Items.VILLAGER_SPAWN_EGG.named("Interact With Entities").setLore(
            "If enabled players will be able to interact with all other entities.".literal()
        )
        value = true
        override = isAdminOverride(minigame, true)
        defaultOptions()
        listener { _, value ->
            canInteractAllSetting.setQuietly(value && canInteractItems.get() && canInteractBlocks.get())
        }
    })

    /**
     * Whether players can interact with blocks.
     */
    @JvmField
    public val canInteractBlocks: GameSetting<Boolean> = this.register(bool {
        name = "can_interact_blocks"
        display = Items.FURNACE.named("Interact With Blocks").setLore(
            "If enabled players will be able to interact with all other blocks".literal()
        )
        value = true
        override = isAdminOverride(minigame, true)
        defaultOptions()
        listener { _, value ->
            canInteractAllSetting.setQuietly(value && canInteractItems.get() && canInteractEntities.get())
        }
    })

    /**
     * Whether players can interact with items.
     */
    @JvmField
    public val canInteractItems: GameSetting<Boolean> = this.register(bool {
        name = "can_interact_items"
        display = Items.WRITTEN_BOOK.named("Interact With Items").hideTooltips().setLore(
            "If enabled players will be able to interact with all items".literal()
        )
        value = true
        override = isAdminOverride(minigame, true)
        defaultOptions()
        listener { _, value ->
            canInteractAllSetting.setQuietly(value && canInteractBlocks.get() && canInteractEntities.get())
        }
    })

    private val canInteractAllSetting = this.register(bool {
        name = "can_interact_all"
        display = Items.OBSERVER.named("Interact With Everything").setLore(
            "If enabled players will be able to interact with blocks, entities, and items.".literal(),
            "If disabled players will no longer be able to interact with them.".literal()
        )
        value = true
        defaultOptions()
        listener { _, value ->
            canInteractBlocks.set(value)
            canInteractEntities.set(value)
            canInteractItems.set(value)
        }
    })

    /**
     * Whether players can interact with anything.
     */
    public var canInteractAll: Boolean by this.canInteractAllSetting

    public var shouldDeleteLevels: Boolean by this.register(bool {
        name = "delete_levels_after_game"
        display = Items.LAVA_BUCKET.named("Delete Worlds After Minigame").setLore(
            "If this minigame is utilising Fantasy's temporary worlds,".literal(),
            "this determines if those worlds will be deleted.".literal()
        )
        value = true
        defaultOptions()
    })

    /**
     * This creates a menu which can be displayed to a
     * player to directly interact with the settings.
     *
     * @return The menu provider.
     */
    override fun menu(): MenuProvider {
        return ScreenUtils.createSettingsMenu(this, ScreenUtils.DefaultMinigameSettingsComponent)
    }

    public companion object {
        public fun <T: Any> isAdminOverride(minigame: Minigame<*>, value: T): (ServerPlayer) -> T? {
            return { if (minigame.isAdmin(it)) value else null }
        }
    }
}