/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.settings

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.managers.MinigameChatManager
import net.casual.arcade.minigame.settings.GameSettingBuilder.Companion.bool
import net.casual.arcade.minigame.utils.defaultOptions
import net.casual.arcade.minigame.utils.item
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
    public val minigame: Minigame,
    title: Component = Component.translatable("minigame.gui.settings")
): GameSettings(title) {
    /**
     * Whether pvp is enabled for this minigame.
     */
    @JvmField
    public val canPvp: GameSetting<Boolean> = this.register(bool {
        name = "pvp"
        display = item(Items.IRON_SWORD, Component.translatable("minigame.settings.canPvp.name"))
            .styledLore(Component.translatable("minigame.settings.canPvp.desc.1"))
        value = true
        defaultOptions()
    })

    /**
     * Whether the player will lose hunger.
     */
    @JvmField
    public val canGetHungry: GameSetting<Boolean> = this.register(bool {
        name = "hunger"
        display = item(Items.COOKED_BEEF, Component.translatable("minigame.settings.canGetHungry.name"))
            .styledLore(Component.translatable("minigame.settings.canGetHungry.desc.1"))
        value = true
        defaultOptions()
    })

    /**
     * Whether players can take damage.
     */
    @JvmField
    public val canTakeDamage: GameSetting<Boolean> = this.register(bool {
        name = "can_take_damage"
        display = item(Items.SHIELD, Component.translatable("minigame.settings.canTakeDamage.name"))
            .styledLore(Component.translatable("minigame.settings.canTakeDamage.desc.1"))
        value = true
        defaultOptions()
    })

    /**
     * Whether players can break blocks.
     */
    @JvmField
    public val canBreakBlocks: GameSetting<Boolean> = this.register(bool {
        name = "can_break_blocks"
        display = item(Items.DIAMOND_PICKAXE, Component.translatable("minigame.settings.canBreakBlocks.name"))
            .styledLore(Component.translatable("minigame.settings.canBreakBlocks.desc.1"))
        value = true
        override(isAdminOverride(true))
        defaultOptions()
    })

    /**
     * Whether players can place blocks.
     */
    @JvmField
    public val canPlaceBlocks: GameSetting<Boolean> = this.register(bool {
        name = "can_place_blocks"
        display = item(Items.DIRT, Component.translatable("minigame.settings.canPlaceBlocks.name"))
            .styledLore(Component.translatable("minigame.settings.canPlaceBlocks.desc.1"))
        value = true
        override(isAdminOverride(true))
        defaultOptions()
    })

    /**
     * Whether players can drop items in this minigame.
     */
    @JvmField
    public val canDropItems: GameSetting<Boolean> = this.register(bool {
        name = "can_drop_items"
        display = item(Items.DIORITE, Component.translatable("minigame.settings.canDropItems.name"))
            .styledLore(Component.translatable("minigame.settings.canDropItems.desc.1"))
        value = true
        override(isAdminOverride(true))
        defaultOptions()
    })

    /**
     * Whether players can pick up items.
     */
    @JvmField
    public val canPickupItems: GameSetting<Boolean> = this.register(bool {
        name = "can_pickup_items"
        display = item(Items.COBBLESTONE, Component.translatable("minigame.settings.canPickupItems.name"))
            .styledLore(Component.translatable("minigame.settings.canPickupItems.desc.1"))
        value = true
        override(isAdminOverride(true))
        defaultOptions()
    })

    /**
     * Whether players can attack entities.
     */
    @JvmField
    public val canAttackEntities: GameSetting<Boolean> = this.register(bool {
        name = "can_attack_entities"
        display = item(Items.DIAMOND_AXE, Component.translatable("minigame.settings.canAttackEntities.name"))
            .styledLore(Component.translatable("minigame.settings.canAttackEntities.desc.1"))
        value = true
        override(isAdminOverride(true))
        defaultOptions()
    })

    /**
     * Whether players can interact with entities.
     */
    @JvmField
    public val canInteractEntities: GameSetting<Boolean> = this.register(bool {
        name = "can_interact_entities"
        display = item(Items.VILLAGER_SPAWN_EGG, Component.translatable("minigame.settings.canInteractEntities.name"))
            .styledLore(Component.translatable("minigame.settings.canInteractEntities.desc.1"))
        value = true
        override(isAdminOverride(true))
        defaultOptions()
    })

    /**
     * Whether players can interact with blocks.
     */
    @JvmField
    public val canInteractBlocks: GameSetting<Boolean> = this.register(bool {
        name = "can_interact_blocks"
        display = item(Items.FURNACE, Component.translatable("minigame.settings.canInteractBlocks.name"))
            .styledLore(Component.translatable("minigame.settings.canInteractBlocks.desc.1"))
        value = true
        override(isAdminOverride(true))
        defaultOptions()
    })

    /**
     * Whether players can interact with items.
     */
    @JvmField
    public val canInteractItems: GameSetting<Boolean> = this.register(bool {
        name = "can_interact_items"
        display = item(Items.WRITTEN_BOOK, Component.translatable("minigame.settings.canInteractItems.name"))
            .styledLore(Component.translatable("minigame.settings.canInteractItems.desc.1"))
        value = true
        override(isAdminOverride(true))
        defaultOptions()
    })

    /**
     * Whether players can interact with blocks, entities, and items.
     */
    public var canInteractAll: Boolean
        get() = this.canInteractBlocks.get() && this.canInteractEntities.get() && this.canInteractItems.get()
        set(value) {
            this.canInteractBlocks.set(value)
            this.canInteractEntities.set(value)
            this.canInteractItems.set(value)
        }

    public var useVanillaChat: Boolean by this.register(bool {
        name = "use_vanilla_chat"
        display = Items.CONCRETE.white.named(Component.translatable("minigame.settings.useVanillaChat.name"))
            .styledLore(Component.translatable("minigame.settings.useVanillaChat.desc.1"), Component.translatable("minigame.settings.useVanillaChat.desc.2"))
        value = false
        defaultOptions()
    })

    public var canCrossChat: Boolean by this.register(bool {
        name = "can_cross_chat"
        display = item(Items.PAPER, Component.translatable("minigame.settings.canCrossChat.name"))
            .styledLore(Component.translatable("minigame.settings.canCrossChat.desc.1"), Component.translatable("minigame.settings.canCrossChat.desc.2"))
        value = false
        defaultOptions()
    })

    public var isChatGlobal: Boolean by this.register(bool {
        name = "is_chat_global"
        display = item(Items.ACACIA_SIGN, Component.translatable("minigame.settings.isChatGlobal.name"))
            .styledLore(Component.translatable("minigame.settings.isChatGlobal.desc.1"), Component.translatable("minigame.settings.isChatGlobal.desc.2"))
        value = true
        onApply { _, _ ->
            minigame.chat.onGlobalChatToggle()
        }
        defaultOptions()
    })

    @JvmField
    public var enableChatCommand: GameSetting<Boolean> = this.register(bool {
        name = "enable_chat_command"
        display = item(Items.COMMAND_BLOCK, Component.translatable("minigame.settings.enableChatCommand.name"))
            .styledLore(Component.translatable("minigame.settings.isChatGlobal.desc.1"))
        value = false
        override(isAdminOverride(true))
        defaultOptions()
    })

    @JvmField
    public val isChatMuted: GameSetting<Boolean> = this.register(bool {
        name = "is_chat_muted"
        display = item(Items.BARRIER, Component.translatable("minigame.settings.isChatMuted.name"))
            .styledLore(Component.translatable("minigame.settings.isChatMuted.desc.1"))
        value = false
        override(::muteOverride)
        defaultOptions()
    })

    public var formatGlobalSystemChat: Boolean by this.register(bool {
        name = "format_global_system_chat"
        display = Items.DYE.yellow.named(Component.translatable("minigame.settings.formatGlobalSystemChat.name"))
            .styledLore(Component.translatable("minigame.settings.formatGlobalSystemChat.desc.1"))
        value = true
        defaultOptions()
    })

    public val tickFreezeOnPause: GameSetting<Boolean> = this.register(bool {
        name = "tick_freeze_on_pause"
        display = item(Items.BLUE_ICE, Component.translatable("minigame.settings.tickFreezeOnPause.name"))
            .styledLore(Component.translatable("minigame.settings.tickFreezeOnPause.desc.1"))
        value = false
        override(isAdminOverride(false))
        defaultOptions()
    })

    public val tickFreezeEntities: GameSetting<Boolean> = this.register(bool {
        name = "freeze_entities"
        display = item(Items.PACKED_ICE, Component.translatable("minigame.settings.freezeEntities.name"))
            .styledLore(Component.translatable("minigame.settings.freezeEntities.desc.1"))
        value = false
        override(isAdminOverride(false))
        defaultOptions()
    })

    public var pauseOnServerStop: Boolean by this.register(bool {
        name = "pause_on_server_stop"
        display = item(Items.ICE, Component.translatable("minigame.settings.pauseOnServerStop.name"))
            .styledLore(Component.translatable("minigame.settings.pauseOnServerStop.desc.1"))
        value = true
        defaultOptions()
    })

    public var canLookAroundWhenFrozen: Boolean by this.register(bool {
        name = "can_look_around_when_frozen"
        display = item(Items.PLAYER_HEAD, Component.translatable("minigame.settings.canLookAroundWhenFrozen.name"))
            .styledLore(Component.translatable("minigame.settings.canLookAroundWhenFrozen.desc.1"))
        value = true
        defaultOptions()
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