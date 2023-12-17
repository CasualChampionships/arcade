package net.casual.arcade.advancements

import net.casual.arcade.events.server.ServerAdvancementReloadEvent
import net.minecraft.advancements.*
import net.minecraft.advancements.critereon.ImpossibleTrigger
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike
import java.util.*

/**
 * This class is a builder for building [Advancement]s.
 *
 * You can then register your advancements using the event
 * [ServerAdvancementReloadEvent].
 *
 * @see Advancement
 */
public class AdvancementBuilder {
    private val criterion = java.util.Map.of<String, Criterion<*>>(
        "Impossible", CriteriaTriggers.IMPOSSIBLE.createCriterion(ImpossibleTrigger.TriggerInstance())
    )

    /**
     * The parent [Advancement], may be null if this has no parent.
     */
    public var parent: ResourceLocation? = null

    /**
     * This is the [ResourceLocation] of the advancement.
     */
    public var id: ResourceLocation? = null

    /**
     * This is the display [ItemStack].
     */
    public var display: ItemStack = ItemStack.EMPTY

    /**
     * The title of the advancement.
     */
    public var title: Component = Component.empty()

    /**
     * The description of the advancement.
     */
    public var description: Component = Component.empty()

    /**
     * The background image for the advancement display.
     */
    public var background: ResourceLocation? = null

    /**
     * The [FrameType] for the advancement.
     */
    public var type: AdvancementType = AdvancementType.TASK

    /**
     * The rewards granted for the advancement.
     */
    public var rewards: AdvancementRewards = AdvancementRewards.EMPTY

    /**
     * Whether the advancement will toast.
     */
    public var toast: Boolean = false

    /**
     * Whether the advancement should be announced in chat.
     */
    public var announce: Boolean = false

    /**
     * Whether the advancement should be hidden from the player.
     */
    public var hidden: Boolean = false

    /**
     * This sets the parent advancement.
     *
     * @param parent The parent advancement.
     * @return This [AdvancementBuilder] instance.
     */
    public fun parent(parent: AdvancementHolder): AdvancementBuilder {
        this.parent = parent.id
        return this
    }

    /**
     * This sets the advancement id.
     *
     * @param id The advancement id.
     * @return This [AdvancementBuilder] instance.
     */
    public fun id(id: ResourceLocation): AdvancementBuilder {
        this.id = id
        return this
    }

    /**
     * This sets the advancement display stack.
     *
     * @param display The advancement display stack.
     * @return This [AdvancementBuilder] instance.
     */
    public fun display(display: ItemStack): AdvancementBuilder {
        this.display = display
        return this
    }

    /**
     * This sets the advancement display stack.
     *
     * @param display The advancement display stack.
     * @return This [AdvancementBuilder] instance.
     */
    public fun display(display: ItemLike): AdvancementBuilder {
        this.display = display.asItem().defaultInstance
        return this
    }

    /**
     * This sets the advancement title.
     *
     * @param title The advancement title.
     * @return This [AdvancementBuilder] instance.
     */
    public fun title(title: Component): AdvancementBuilder {
        this.title = title
        return this
    }

    /**
     * This sets the advancement description.
     *
     * @param description The advancement description.
     * @return This [AdvancementBuilder] instance.
     */
    public fun description(description: Component): AdvancementBuilder {
        this.description = description
        return this
    }

    /**
     * This sets the advancement background.
     *
     * @param background The advancement background.
     * @return This [AdvancementBuilder] instance.
     */
    public fun background(background: ResourceLocation): AdvancementBuilder {
        this.background = background
        return this
    }

    /**
     * This sets the advancement type.
     *
     * @param type The advancement type.
     * @return This [AdvancementBuilder] instance.
     */
    public fun type(type: AdvancementType): AdvancementBuilder {
        this.type = type
        return this
    }

    /**
     * This sets the advancement rewards.
     *
     * @param rewards The advancement rewards.
     * @return This [AdvancementBuilder] instance.
     */
    public fun rewards(rewards: AdvancementRewards): AdvancementBuilder {
        this.rewards = rewards
        return this
    }

    /**
     * This sets whether the advancement toasts.
     *
     * @return This [AdvancementBuilder] instance.
     */
    public fun toast(): AdvancementBuilder {
        this.toast = true
        return this
    }

    /**
     * This sets whether the advancement announces.
     *
     * @return This [AdvancementBuilder] instance.
     */
    public fun announce(): AdvancementBuilder {
        this.announce = true
        return this
    }

    /**
     * This sets whether the advancement is hidden.
     *
     * @return This [AdvancementBuilder] instance.
     */
    public fun hidden(): AdvancementBuilder {
        this.hidden = true
        return this
    }

    /**
     * This builds the [Advancement] from this builder.
     *
     * This should be the last step of the builder.
     * You must make sure you have a valid `id` before
     * you build your advancement.
     *
     * @return The built [Advancement].
     */
    public fun build(): AdvancementHolder {
        val id = requireNotNull(this.id)
        val advancement = Advancement(
            Optional.ofNullable(this.parent),
            Optional.of(DisplayInfo(
                this.display,
                this.title,
                this.description,
                Optional.ofNullable(this.background),
                this.type,
                this.toast,
                this.announce,
                this.hidden
            )),
            this.rewards,
            this.criterion,
            AdvancementRequirements.Strategy.OR.create(this.criterion.keys),
            false
        )
        return AdvancementHolder(id, advancement)
    }

    public companion object {
        /**
         * This creates an [AdvancementBuilder].
         */
        public fun create(block: AdvancementBuilder.() -> Unit): AdvancementBuilder {
            return AdvancementBuilder().apply(block)
        }
    }
}