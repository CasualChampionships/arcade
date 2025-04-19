/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.mixins;

import net.casual.arcade.border.ducks.SerializableBorder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.border.WorldBorder;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldBorder.class)
public abstract class WorldBorderMixin implements SerializableBorder {
	@Shadow private double damagePerBlock;
	@Shadow private double damageSafeZone;
	@Shadow private int warningBlocks;
	@Shadow private int warningTime;

	@Shadow public abstract double getSize();

	@Shadow public abstract long getLerpRemainingTime();

	@Shadow public abstract double getLerpTarget();

	@Shadow public abstract void setCenter(double x, double z);

	@Shadow public abstract double getCenterX();

	@Shadow public abstract double getCenterZ();

	@Shadow public abstract void lerpSizeBetween(double oldSize, double newSize, long time);

	@Shadow public abstract void setSize(double size);

	@Shadow public abstract int getWarningBlocks();

	@Shadow public abstract int getWarningTime();

	@Shadow public abstract double getDamageSafeZone();

	@Shadow public abstract double getDamagePerBlock();

	@Override
	public CompoundTag arcade$serialize() {
		CompoundTag compound = new CompoundTag();
		compound.putDouble("center_x", this.getCenterX());
		compound.putDouble("center_z", this.getCenterZ());
		compound.putDouble("size", this.getSize());
		compound.putDouble("damage_safe_zone", this.getDamageSafeZone());
		compound.putDouble("damage_per_block", this.getDamagePerBlock());
		compound.putLong("lerp_time", this.getLerpRemainingTime());
		compound.putDouble("lerp_target", this.getLerpTarget());
		compound.putInt("warning_blocks", this.getWarningBlocks());
		compound.putInt("warning_time", this.getWarningTime());
		return compound;
	}

	@Override
	public void arcade$deserialize(@NotNull CompoundTag compound) {
		WorldBorder.Settings settings = WorldBorder.DEFAULT_SETTINGS;
		this.setCenter(
			compound.getDoubleOr("center_x", settings.getCenterX()),
			compound.getDoubleOr("center_z", settings.getCenterZ())
		);
		this.damagePerBlock = compound.getDoubleOr("damage_per_block", settings.getDamagePerBlock());
		this.damageSafeZone = compound.getDoubleOr("damage_safe_zone", settings.getSafeZone());
		this.warningBlocks = compound.getIntOr("warning_blocks", settings.getWarningBlocks());
		this.warningTime = compound.getIntOr("warning_time", settings.getWarningTime());
		long remaining = compound.getLongOr("lerp_time", settings.getSizeLerpTime());
		double size = compound.getDoubleOr("size", settings.getSize());
		if (remaining > 0L) {
			this.lerpSizeBetween(size, compound.getDoubleOr("lerp_target", settings.getSizeLerpTarget()), remaining);
		} else {
			this.setSize(size);
		}
	}
}
