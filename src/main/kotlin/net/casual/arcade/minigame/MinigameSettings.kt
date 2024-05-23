package net.casual.arcade.minigame

import net.casual.arcade.minigame.managers.MinigameChatManager
import net.casual.arcade.settings.GameSetting
import net.casual.arcade.settings.display.DisplayableSettings
import net.casual.arcade.settings.display.DisplayableSettingsDefaults
import net.casual.arcade.settings.display.MenuGameSettingBuilder.Companion.bool
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ItemUtils.hideTooltips
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.ItemUtils.styledLore
import net.minecraft.server.level.ServerPlayer
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
 * `/minigame settings <minigame-uuid> <setting> set from option <option>`
 *
 * `/minigame settings <minigame-uuid> <setting> set from value <value>`
 *
 */
public open class MinigameSettings(
    private val minigame: Minigame<*>,
    defaults: DisplayableSettingsDefaults = DisplayableSettingsDefaults()
): DisplayableSettings(defaults) {
    /**
     * Whether pvp is enabled for this minigame.
     */
    @JvmField
    public val canPvp: GameSetting<Boolean> = this.register(bool {
        name = "pvp"
        display = Items.IRON_SWORD.named("PvP").styledLore(
            "If enabled this will allow players to pvp with one another.".literal()
        )
        value = true
        defaults.options(this)
    })

    /**
     * Whether the player will lose hunger.
     */
    @JvmField
    public val canGetHungry: GameSetting<Boolean> = this.register(bool {
        name = "hunger"
        display = Items.COOKED_BEEF.named("Hunger").styledLore(
            "If enabled this will cause players to lose hunger over time.".literal()
        )
        value = true
        defaults.options(this)
    })

    /**
     * Whether players can take damage.
     */
    @JvmField
    public val canTakeDamage: GameSetting<Boolean> = this.register(bool {
        name = "can_take_damage"
        display = Items.SHIELD.named("Damage").styledLore(
            "If enabled players will be able to take damage.".literal()
        )
        value = true
        defaults.options(this)
    })

    /**
     * Whether players can break blocks.
     */
    @JvmField
    public val canBreakBlocks: GameSetting<Boolean> = this.register(bool {
        name = "can_break_blocks"
        display = Items.DIAMOND_PICKAXE.named("Break Blocks").styledLore(
            "If enabled players will be able to break blocks.".literal()
        )
        value = true
        override = isAdminOverride(true)
        defaults.options(this)
    })

    /**
     * Whether players can place blocks.
     */
    @JvmField
    public val canPlaceBlocks: GameSetting<Boolean> = this.register(bool {
        name = "can_place_blocks"
        display = Items.DIRT.named("Place Blocks").styledLore(
            "If enabled players will be able to place blocks.".literal()
        )
        value = true
        override = isAdminOverride(true)
        defaults.options(this)
    })

    /**
     * Whether players can drop items in this minigame
     */
    @JvmField
    public val canDropItems: GameSetting<Boolean> = this.register(bool {
        name = "can_drop_items"
        display = Items.DIORITE.named("Drop Items").styledLore(
            "If enabled players will be able to drop items".literal()
        )
        value = true
        override = isAdminOverride(true)
        defaults.options(this)
    })

    /**
     * Whether players can pick up items.
     */
    @JvmField
    public val canPickupItems: GameSetting<Boolean> = this.register(bool {
        name = "can_pickup_items"
        display = Items.COBBLESTONE.named("Pickup Items").styledLore(
            "If enabled players will be able to pick up items.".literal()
        )
        value = true
        override = isAdminOverride(true)
        defaults.options(this)
    })

    /**
     * Whether players can attack entities.
     */
    @JvmField
    public val canAttackEntities: GameSetting<Boolean> = this.register(bool {
        name = "can_attack_entities"
        display = Items.DIAMOND_AXE.named("Attack Entities").styledLore(
            "If enabled players will be able to attack all other entities.".literal()
        )
        value = true
        override = isAdminOverride(true)
        defaults.options(this)
    })

    /**
     * Whether players can interact with entities.
     */
    @JvmField
    public val canInteractEntities: GameSetting<Boolean> = this.register(bool {
        name = "can_interact_entities"
        display = Items.VILLAGER_SPAWN_EGG.named("Interact With Entities").styledLore(
            "If enabled players will be able to interact with all other entities.".literal()
        )
        value = true
        override = isAdminOverride(true)
        defaults.options(this)
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
        display = Items.FURNACE.named("Interact With Blocks").styledLore(
            "If enabled players will be able to interact with all other blocks".literal()
        )
        value = true
        override = isAdminOverride(true)
        defaults.options(this)
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
        display = Items.WRITTEN_BOOK.named("Interact With Items").styledLore(
            "If enabled players will be able to interact with all items".literal()
        )
        value = true
        override = isAdminOverride(true)
        defaults.options(this)
        listener { _, value ->
            canInteractAllSetting.setQuietly(value && canInteractBlocks.get() && canInteractEntities.get())
        }
    })

    private val canInteractAllSetting = this.register(bool {
        name = "can_interact_all"
        display = Items.OBSERVER.named("Interact With Everything").styledLore(
            "If enabled players will be able to interact with blocks, entities, and items.".literal(),
            "If disabled players will no longer be able to interact with them.".literal()
        )
        value = true
        defaults.options(this)
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
        display = Items.LAVA_BUCKET.named("Delete Worlds After Minigame").styledLore(
            "If this minigame is utilising Fantasy's temporary worlds,".literal(),
            "this determines if those worlds will be deleted.".literal()
        )
        value = true
        defaults.options(this)
    })

    public var useVanillaChat: Boolean by this.register(bool {
        name = "use_vanilla_chat"
        display = Items.WHITE_CONCRETE.named("Use Vanilla Chat").styledLore(
            "If this is enabled then vanilla chat is used for".literal(),
            "the minigame chat. Global Chat must also be enabled.".literal()
        )
        value = false
        defaults.options(this)
    })

    public var canCrossChat: Boolean by this.register(bool {
        name = "can_cross_chat"
        display = Items.PAPER.named("Cross Chat").styledLore(
            "If this is enabled then the entire server can see".literal(),
            "chat messages from players in the minigame, and".literal(),
            "players in the minigame can see global messages.".literal()
        )
        value = false
        defaults.options(this)
    })

    public var isChatGlobal: Boolean by this.register(bool {
        name = "is_chat_global"
        display = Items.ACACIA_SIGN.named("Global Chat").styledLore(
            "If disabled then by default messages will be sent to teams.".literal(),
            "To send messages to global players must prefix their message with !.".literal()
        )
        value = true
        defaults.options(this)
    })

    @JvmField
    public val isChatMuted: GameSetting<Boolean> = this.register(bool {
        name = "is_chat_muted"
        display = Items.BARRIER.named("Mute Chat").styledLore(
            "If enabled, players will not be able to talk in chat".literal()
        )
        value = false
        override = ::muteOverride
        defaults.options(this)
    })

    public var mobsWithNoAIAreFlammable: Boolean by this.register(bool {
        name = "mobs_with_no_ai_are_flammable"
        display = Items.FLINT_AND_STEEL.named("Mobs With No AI Are Flammable").styledLore(
            "If enabled mobs with no ai can be set on fire".literal()
        )
        value = false
        defaults.options(this)
    })

    public val tickFreezeOnPause: GameSetting<Boolean> = this.register(bool {
        name = "tick_freeze_on_pause"
        display = Items.BLUE_ICE.named("Tick Freeze On Pause").styledLore(
            "When the minigame is paused the world will".literal(),
            "freeze all ticking, including players.".literal()
        )
        value = false
        override = isAdminOverride(false)
        defaults.options(this)
    })

    public val freezeEntities: GameSetting<Boolean> = this.register(bool {
        name = "freeze_entities"
        display = Items.PACKED_ICE.named("Freeze Entities").styledLore(
            "This will freeze all entities including players.".literal(),
        )
        value = false
        override = isAdminOverride(false)
        defaults.options(this)
    })

    public var pauseOnServerStop: Boolean by this.register(bool {
        name = "pause_on_server_stop"
        display = Items.ICE.named("Pause On Server Stop").styledLore(
            "Pauses the minigame just before the server stops".literal()
        )
        value = true
        defaults.options(this)
    })

    public var canLookAroundWhenFrozen: Boolean by this.register(bool {
        name = "can_look_around_when_frozen"
        display = Items.PLAYER_HEAD.named("Can Look Around When Frozen").styledLore(
            "Allows players to look around while frozen".literal()
        )
        value = true
        defaults.options(this)
    })

    protected fun <T: Any> isAdminOverride(value: T): (ServerPlayer) -> T? {
        return { if (this.minigame.players.isAdmin(it)) value else null }
    }

    protected fun muteOverride(player: ServerPlayer): Boolean? {
        if (this.minigame.players.isAdmin(player)) {
            return false
        }
        if (this.minigame.tags.has(player, MinigameChatManager.MUTED)) {
            return true
        }
        return null
    }
}