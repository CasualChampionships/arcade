/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.settings

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.managers.MinigameChatManager
import net.casual.arcade.minigame.settings.display.DisplayableSettings
import net.casual.arcade.minigame.settings.display.DisplayableSettingsDefaults
import net.casual.arcade.minigame.settings.display.MenuGameSettingBuilder.Companion.bool
import net.casual.arcade.minigame.settings.display.MenuGameSettingBuilder.Companion.int32
import net.casual.arcade.utils.ItemUtils
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.ItemUtils.styledLore
import net.minecraft.network.chat.Component
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
    internal val minigame: Minigame,
    defaults: DisplayableSettingsDefaults = DisplayableSettingsDefaults()
): DisplayableSettings(defaults) {
    /**
     * Whether pvp is enabled for this minigame.
     */
    @JvmField
    public val canPvp: GameSetting<Boolean> = this.register(bool {
        name = "pvp"
        display = Items.IRON_SWORD.named(Component.translatable("minigame.settings.canPvp.name"))
            .styledLore(Component.translatable("minigame.settings.canPvp.desc.1"))
        value = true
        defaults.options(this)
    })

    /**
     * Whether the player will lose hunger.
     */
    @JvmField
    public val canGetHungry: GameSetting<Boolean> = this.register(bool {
        name = "hunger"
        display = Items.COOKED_BEEF.named(Component.translatable("minigame.settings.canGetHungry.name"))
            .styledLore(Component.translatable("minigame.settings.canGetHungry.desc.1"))
        value = true
        defaults.options(this)
    })

    /**
     * Whether players can take damage.
     */
    @JvmField
    public val canTakeDamage: GameSetting<Boolean> = this.register(bool {
        name = "can_take_damage"
        display = Items.SHIELD.named(Component.translatable("minigame.settings.canTakeDamage.name"))
            .styledLore(Component.translatable("minigame.settings.canTakeDamage.desc.1"))
        value = true
        defaults.options(this)
    })

    /**
     * Whether players can break blocks.
     */
    @JvmField
    public val canBreakBlocks: GameSetting<Boolean> = this.register(bool {
        name = "can_break_blocks"
        display = Items.DIAMOND_PICKAXE.named(Component.translatable("minigame.settings.canBreakBlocks.name"))
            .styledLore(Component.translatable("minigame.settings.canBreakBlocks.desc.1"))
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
        display = Items.DIRT.named(Component.translatable("minigame.settings.canPlaceBlocks.name"))
            .styledLore(Component.translatable("minigame.settings.canPlaceBlocks.desc.1"))
        value = true
        override = isAdminOverride(true)
        defaults.options(this)
    })

    /**
     * Whether players can drop items in this minigame.
     */
    @JvmField
    public val canDropItems: GameSetting<Boolean> = this.register(bool {
        name = "can_drop_items"
        display = Items.DIORITE.named(Component.translatable("minigame.settings.canDropItems.name"))
            .styledLore(Component.translatable("minigame.settings.canDropItems.desc.1"))
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
        display = Items.COBBLESTONE.named(Component.translatable("minigame.settings.canPickupItems.name"))
            .styledLore(Component.translatable("minigame.settings.canPickupItems.desc.1"))
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
        display = Items.DIAMOND_AXE.named(Component.translatable("minigame.settings.canAttackEntities.name"))
            .styledLore(Component.translatable("minigame.settings.canAttackEntities.desc.1"))
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
        display = Items.VILLAGER_SPAWN_EGG.named(Component.translatable("minigame.settings.canInteractEntities.name"))
            .styledLore(Component.translatable("minigame.settings.canInteractEntities.desc.1"))
        value = true
        override = isAdminOverride(true)
        defaults.options(this)
        listener { _, _, value ->
            canInteractAllSetting.setQuietly(value && canInteractItems.get() && canInteractBlocks.get())
        }
    })

    /**
     * Whether players can interact with blocks.
     */
    @JvmField
    public val canInteractBlocks: GameSetting<Boolean> = this.register(bool {
        name = "can_interact_blocks"
        display = Items.FURNACE.named(Component.translatable("minigame.settings.canInteractBlocks.name"))
            .styledLore(Component.translatable("minigame.settings.canInteractBlocks.desc.1"))
        value = true
        override = isAdminOverride(true)
        defaults.options(this)
        listener { _, _, value ->
            canInteractAllSetting.setQuietly(value && canInteractItems.get() && canInteractEntities.get())
        }
    })

    /**
     * Whether players can interact with items.
     */
    @JvmField
    public val canInteractItems: GameSetting<Boolean> = this.register(bool {
        name = "can_interact_items"
        display = Items.WRITTEN_BOOK.named(Component.translatable("minigame.settings.canInteractItems.name"))
            .styledLore(Component.translatable("minigame.settings.canInteractItems.desc.1"))
        value = true
        override = isAdminOverride(true)
        defaults.options(this)
        listener { _, _, value ->
            canInteractAllSetting.setQuietly(value && canInteractBlocks.get() && canInteractEntities.get())
        }
    })

    private val canInteractAllSetting = this.register(bool {
        name = "can_interact_all"
        display = Items.OBSERVER.named(Component.translatable("minigame.settings.canInteractAll.name"))
            .styledLore(Component.translatable("minigame.settings.canInteractAll.desc.1"), Component.translatable("minigame.settings.canInteractAll.desc.2"))
        value = true
        defaults.options(this)
        listener { _, _, value ->
            canInteractBlocks.set(value)
            canInteractEntities.set(value)
            canInteractItems.set(value)
        }
    })

    /**
     * Whether players can interact with anything.
     */
    public var canInteractAll: Boolean by this.canInteractAllSetting

    public var useVanillaChat: Boolean by this.register(bool {
        name = "use_vanilla_chat"
        display = Items.WHITE_CONCRETE.named(Component.translatable("minigame.settings.useVanillaChat.name"))
            .styledLore(Component.translatable("minigame.settings.useVanillaChat.desc.1"), Component.translatable("minigame.settings.useVanillaChat.desc.2"))
        value = false
        defaults.options(this)
    })

    public var canCrossChat: Boolean by this.register(bool {
        name = "can_cross_chat"
        display = Items.PAPER.named(Component.translatable("minigame.settings.canCrossChat.name"))
            .styledLore(Component.translatable("minigame.settings.canCrossChat.desc.1"), Component.translatable("minigame.settings.canCrossChat.desc.2"))
        value = false
        defaults.options(this)
    })

    public var isChatGlobal: Boolean by this.register(bool {
        name = "is_chat_global"
        display = Items.ACACIA_SIGN.named(Component.translatable("minigame.settings.isChatGlobal.name"))
            .styledLore(Component.translatable("minigame.settings.isChatGlobal.desc.1"), Component.translatable("minigame.settings.isChatGlobal.desc.2"))
        value = true
        listener { setting, _, value ->
            setting.setQuietly(value)
            minigame.chat.onGlobalChatToggle()
        }
        defaults.options(this)
    })

    @JvmField
    public var enableChatCommand: GameSetting<Boolean> = this.register(bool {
        name = "enable_chat_command"
        display = Items.COMMAND_BLOCK.named(Component.translatable("minigame.settings.enableChatCommand.name"))
            .styledLore(Component.translatable("minigame.settings.isChatGlobal.desc.1"))
        value = false
        override = isAdminOverride(true)
        defaults.options(this)
    })

    @JvmField
    public val isChatMuted: GameSetting<Boolean> = this.register(bool {
        name = "is_chat_muted"
        display = Items.BARRIER.named(Component.translatable("minigame.settings.isChatMuted.name"))
            .styledLore(Component.translatable("minigame.settings.isChatMuted.desc.1"))
        value = false
        override = ::muteOverride
        defaults.options(this)
    })

    public var formatGlobalSystemChat: Boolean by this.register(bool {
        name = "format_global_system_chat"
        display = Items.YELLOW_DYE.named(Component.translatable("minigame.settings.formatGlobalSystemChat.name"))
            .styledLore(Component.translatable("minigame.settings.formatGlobalSystemChat.desc.1"))
        value = true
        defaults.options(this)
    })

    public var mobsWithNoAIAreFlammable: Boolean by this.register(bool {
        name = "mobs_with_no_ai_are_flammable"
        display = Items.FLINT_AND_STEEL.named(Component.translatable("minigame.settings.mobsWithNoAIAreFlammable.name"))
            .styledLore(Component.translatable("minigame.settings.mobsWithNoAIAreFlammable.desc.1"))
        value = false
        defaults.options(this)
    })

    public val tickFreezeOnPause: GameSetting<Boolean> = this.register(bool {
        name = "tick_freeze_on_pause"
        display = Items.BLUE_ICE.named(Component.translatable("minigame.settings.tickFreezeOnPause.name"))
            .styledLore(Component.translatable("minigame.settings.tickFreezeOnPause.desc.1"))
        value = false
        override = isAdminOverride(false)
        defaults.options(this)
    })

    public val freezeEntities: GameSetting<Boolean> = this.register(bool {
        name = "freeze_entities"
        display = Items.PACKED_ICE.named(Component.translatable("minigame.settings.freezeEntities.name"))
            .styledLore(Component.translatable("minigame.settings.freezeEntities.desc.1"))
        value = false
        override = isAdminOverride(false)
        defaults.options(this)
    })

    public var pauseOnServerStop: Boolean by this.register(bool {
        name = "pause_on_server_stop"
        display = Items.ICE.named(Component.translatable("minigame.settings.pauseOnServerStop.name"))
            .styledLore(Component.translatable("minigame.settings.pauseOnServerStop.desc.1"))
        value = true
        defaults.options(this)
    })

    public var canLookAroundWhenFrozen: Boolean by this.register(bool {
        name = "can_look_around_when_frozen"
        display = Items.PLAYER_HEAD.named(Component.translatable("minigame.settings.canLookAroundWhenFrozen.name"))
            .styledLore(Component.translatable("minigame.settings.canLookAroundWhenFrozen.desc.1"))
        value = true
        defaults.options(this)
    })

    public var daylightCycle: Int by this.register(int32 {
        name = "daylight_cycle"
        display = Items.CLOCK.named(Component.translatable("minigame.settings.daylightCycle.name"))
            .styledLore(Component.translatable("minigame.settings.daylightCycle.desc.1"))
        value = 1
        option("none", ItemUtils.light(0).named(Component.translatable("minigame.settings.daylightCycle.option.none")), 0)
        option("normal", ItemUtils.light(1).named(Component.translatable("minigame.settings.daylightCycle.option.normal")), 1)
        option("double", ItemUtils.light(2).named(Component.translatable("minigame.settings.daylightCycle.option.double")), 2)
        option("triple", ItemUtils.light(3).named(Component.translatable("minigame.settings.daylightCycle.option.triple")), 3)
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