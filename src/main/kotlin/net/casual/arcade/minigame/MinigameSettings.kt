package net.casual.arcade.minigame

import net.casual.arcade.settings.DisplayableGameSettingBuilder
import net.casual.arcade.settings.GameSetting
import net.casual.arcade.utils.ItemUtils.hideTooltips
import net.casual.arcade.utils.ItemUtils.literalNamed
import net.casual.arcade.utils.SettingsUtils.defaultOptions
import net.minecraft.world.item.Items

public open class MinigameSettings(
    public val minigame: Minigame<*>
) {
    /**
     * Whether pvp is enabled for this minigame.
     *
     * It is implemented as a [GameSetting] so that it can be
     * changed in the minigame settings GUI.
     */
    public var canPvp: Boolean by this.minigame.registerSetting(
        DisplayableGameSettingBuilder.boolean()
            .name("pvp")
            .display(Items.IRON_SWORD.literalNamed("PvP").hideTooltips())
            .defaultOptions()
            .value(true)
            .build()
    )

    /**
     * Whether the player will lose hunger in this minigame.
     *
     * It is implemented as a [GameSetting] so that it can be
     * changed in the minigame settings GUI.
     */
    public var canGetHungry: Boolean by this.minigame.registerSetting(
        DisplayableGameSettingBuilder.boolean()
            .name("hunger")
            .display(Items.COOKED_BEEF.literalNamed("Hunger"))
            .defaultOptions()
            .value(true)
            .build()
    )

    public var canTakeDamage: Boolean by this.minigame.registerSetting(
        DisplayableGameSettingBuilder.boolean()
            .name("can_take_damage")
            .display(Items.SHIELD.literalNamed("Damage"))
            .defaultOptions()
            .value(true)
            .build()
    )

    public var canBreakBlocks: Boolean by this.minigame.registerSetting(
        DisplayableGameSettingBuilder.boolean()
            .name("can_break_blocks")
            .display(Items.DIAMOND_PICKAXE.literalNamed("Break Blocks"))
            .defaultOptions()
            .value(true)
            .build()
    )

    public var canPlaceBlocks: Boolean by this.minigame.registerSetting(
        DisplayableGameSettingBuilder.boolean()
            .name("can_place_blocks")
            .display(Items.DIRT.literalNamed("Place Blocks"))
            .defaultOptions()
            .value(true)
            .build()
    )

    public var canThrowItems: Boolean by this.minigame.registerSetting(
        DisplayableGameSettingBuilder.boolean()
            .name("can_throw_items")
            .display(Items.ENDER_PEARL.literalNamed("Throw Items"))
            .defaultOptions()
            .value(true)
            .build()
    )

    public var canPickupItems: Boolean by this.minigame.registerSetting(
        DisplayableGameSettingBuilder.boolean()
            .name("can_pickup_items")
            .display(Items.COBBLESTONE.literalNamed("Pickup Items"))
            .defaultOptions()
            .value(true)
            .build()
    )

    public var canAttackEntities: Boolean by this.minigame.registerSetting(
        DisplayableGameSettingBuilder.boolean()
            .name("can_attack_entities")
            .display(Items.DIAMOND_AXE.literalNamed("Attack Entities").hideTooltips())
            .defaultOptions()
            .value(true)
            .build()
    )

    public var canInteractEntities: Boolean by this.minigame.registerSetting(
        DisplayableGameSettingBuilder.boolean()
            .name("can_interact_entities")
            .display(Items.VILLAGER_SPAWN_EGG.literalNamed("Interact With Entities"))
            .defaultOptions()
            .value(true)
            .listener { _, value ->
                this.canInteractAllSetting.setQuietly(value && this.canInteractItems && this.canInteractBlocks)
            }
            .build()
    )

    public var canInteractBlocks: Boolean by this.minigame.registerSetting(
        DisplayableGameSettingBuilder.boolean()
            .name("can_interact_blocks")
            .display(Items.FURNACE.literalNamed("Interact With Blocks"))
            .defaultOptions()
            .value(true)
            .listener { _, value ->
                this.canInteractAllSetting.setQuietly(value && this.canInteractItems && this.canInteractEntities)
            }
            .build()
    )

    public var canInteractItems: Boolean by this.minigame.registerSetting(
        DisplayableGameSettingBuilder.boolean()
            .name("can_interact_items")
            .display(Items.WRITTEN_BOOK.literalNamed("Interact With Items").hideTooltips())
            .defaultOptions()
            .value(true)
            .listener { _, value ->
                this.canInteractAllSetting.setQuietly(value && this.canInteractBlocks && this.canInteractEntities)
            }
            .build()
    )

    private val canInteractAllSetting = this.minigame.registerSetting(
        DisplayableGameSettingBuilder.boolean()
            .name("can_interact_all")
            .display(Items.OBSERVER.literalNamed("Interact With Everything"))
            .defaultOptions()
            .value(true)
            .listener { _, value ->
                this.canInteractBlocks = value
                this.canInteractEntities = value
                this.canInteractItems = value
            }
            .build()
    )

    public var canInteractAll: Boolean by this.canInteractAllSetting
}