package net.casual.arcade.minigame

import net.casual.arcade.settings.GameSetting
import net.casual.arcade.settings.display.DisplayableGameSettingBuilder.Companion.bool
import net.casual.arcade.settings.display.DisplayableSettings
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ItemUtils.hideTooltips
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.ItemUtils.setLore
import net.casual.arcade.utils.ScreenUtils
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
        override = isAdminOverride(true)
        defaultOptionsFor(this)
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
        defaultOptionsFor(this)
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
        defaultOptionsFor(this)
    })

    /**
     * Whether players can break blocks.
     */
    @JvmField
    public val canBreakBlocks: GameSetting<Boolean> = this.register(bool {
        name = "can_break_blocks"
        display = Items.DIAMOND_PICKAXE.named("Break Blocks").hideTooltips().setLore(
            "If enabled players will be able to break blocks.".literal()
        )
        value = true
        override = isAdminOverride(true)
        defaultOptionsFor(this)
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
        override = isAdminOverride(true)
        defaultOptionsFor(this)
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
        override = isAdminOverride(true)
        defaultOptionsFor(this)
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
        override = isAdminOverride(true)
        defaultOptionsFor(this)
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
        override = isAdminOverride(true)
        defaultOptionsFor(this)
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
        override = isAdminOverride(true)
        defaultOptionsFor(this)
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
        override = isAdminOverride(true)
        defaultOptionsFor(this)
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
        override = isAdminOverride(true)
        defaultOptionsFor(this)
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
        defaultOptionsFor(this)
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
        defaultOptionsFor(this)
    })

    public var isChatGlobal: Boolean by this.register(bool {
        name = "is_chat_global"
        display = Items.PAPER.named("Global Chat").setLore(
            "If this is enabled then the entire server can see".literal(),
            "chat messages from players in the minigame, and".literal(),
            "players in the minigame can see global messages.".literal()
        )
        value = false
        defaultOptionsFor(this)
    })

    public var isTeamChat: Boolean by this.register(bool {
        name = "is_team_chat"
        display = Items.ACACIA_SIGN.named("Team Chat").setLore(
            "If enabled then by default messages will be sent to teams.".literal(),
            "To send messages to global players must prefix their message with !.".literal()
        )
        value = false
        defaultOptionsFor(this)
    })

    @JvmField
    public val isChatMuted: GameSetting<Boolean> = this.register(bool {
        name = "is_chat_muted"
        display = Items.BARRIER.named("Mute Chat").setLore(
            "If enabled, players will not be able to talk in chat".literal()
        )
        value = false
        override = isAdminOverride(false)
        defaultOptionsFor(this)
    })

    public var mobsWithNoAIAreFlammable: Boolean by this.register(bool {
        name = "mobs_with_no_ai_are_flammable"
        display = Items.FLINT_AND_STEEL.named("Mobs With No AI Are Flammable").setLore(
            "If enabled mobs with no ai can be set on fire".literal()
        )
        value = false
        defaultOptionsFor(this)
    })

    public val tickFreezeOnPause: GameSetting<Boolean> = this.register(bool {
        name = "tick_freeze_on_pause"
        display = Items.PACKED_ICE.named("Tick Freeze On Pause").setLore(
            "When the minigame is paused the world will".literal(),
            "freeze all ticking, including players.".literal()
        )
        value = false
        override = isAdminOverride(false)
        defaultOptionsFor(this)
    })

    public var pauseOnServerStop: Boolean by this.register(bool {
        name = "pause_on_server_stop"
        display = Items.ICE.named("Pause On Server Stop").setLore(
            "Pauses the minigame just before the server stops".literal()
        )
        value = true
        defaultOptionsFor(this)
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

    protected fun <T: Any> isAdminOverride(value: T): (ServerPlayer) -> T? {
        return { if (this.minigame.isAdmin(it)) value else null }
    }
}