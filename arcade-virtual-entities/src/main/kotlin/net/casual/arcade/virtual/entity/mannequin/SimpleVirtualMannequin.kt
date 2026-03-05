/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.mannequin

import net.casual.arcade.virtual.entity.SimpleVirtualEntity
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.tracker.ObserverTracker
import net.casual.arcade.virtual.entity.tracker.SimpleObserverTracker
import net.casual.arcade.virtual.entity.utils.EntityDataAccessors
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.world.item.component.ResolvableProfile
import java.util.Optional

public open class SimpleVirtualMannequin(
    attachment: VirtualEntityAttachment,
    observers: ObserverTracker = SimpleObserverTracker()
): SimpleVirtualEntity(EntityType.MANNEQUIN, attachment, observers) {
    public fun setMainHand(hand: HumanoidArm) {
        this.setDataEntry(EntityDataAccessors.Avatar.MAIN_HAND, hand)
    }

    public fun setModelCustomization(customization: Byte) {
        this.setDataEntry(EntityDataAccessors.Avatar.MODEL_CUSTOMIZATION, customization)
    }

    public fun setModelCustomizationFor(observer: ServerPlayer, customization: Byte) {
        this.setDataEntryFor(observer, EntityDataAccessors.Avatar.MODEL_CUSTOMIZATION, customization)
    }

    public fun setProfile(profile: ResolvableProfile) {
        this.setDataEntry(EntityDataAccessors.Mannequin.PROFILE, profile)
    }

    public fun setProfileFor(observer: ServerPlayer, profile: ResolvableProfile) {
        this.setDataEntryFor(observer, EntityDataAccessors.Mannequin.PROFILE, profile)
    }

    public fun setImmovable(immovable: Boolean) {
        this.setDataEntry(EntityDataAccessors.Mannequin.IMMOVABLE, immovable)
    }

    public fun setDescription(description: Component?) {
        this.setDataEntry(EntityDataAccessors.Mannequin.DESCRIPTION, Optional.ofNullable(description))
    }

    public fun setDescriptionFor(observer: ServerPlayer, description: Component?) {
        this.setDataEntryFor(observer, EntityDataAccessors.Mannequin.DESCRIPTION, Optional.ofNullable(description))
    }
}