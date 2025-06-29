/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.mixins;

import net.casual.arcade.border.ducks.SerializableBorder;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
	public void arcade$serialize(ValueOutput output) {
		output.putDouble("center_x", this.getCenterX());
		output.putDouble("center_z", this.getCenterZ());
		output.putDouble("size", this.getSize());
		output.putDouble("damage_safe_zone", this.getDamageSafeZone());
		output.putDouble("damage_per_block", this.getDamagePerBlock());
		output.putLong("lerp_time", this.getLerpRemainingTime());
		output.putDouble("lerp_target", this.getLerpTarget());
		output.putInt("warning_blocks", this.getWarningBlocks());
		output.putInt("warning_time", this.getWarningTime());
	}

	@Override
	public void arcade$deserialize(ValueInput input) {
		WorldBorder.Settings settings = WorldBorder.DEFAULT_SETTINGS;
		this.setCenter(
			input.getDoubleOr("center_x", settings.getCenterX()),
			input.getDoubleOr("center_z", settings.getCenterZ())
		);
		this.damagePerBlock = input.getDoubleOr("damage_per_block", settings.getDamagePerBlock());
		this.damageSafeZone = input.getDoubleOr("damage_safe_zone", settings.getSafeZone());
		this.warningBlocks = input.getIntOr("warning_blocks", settings.getWarningBlocks());
		this.warningTime = input.getIntOr("warning_time", settings.getWarningTime());
		long remaining = input.getLongOr("lerp_time", settings.getSizeLerpTime());
		double size = input.getDoubleOr("size", settings.getSize());
		if (remaining > 0L) {
			this.lerpSizeBetween(size, input.getDoubleOr("lerp_target", settings.getSizeLerpTarget()), remaining);
		} else {
			this.setSize(size);
		}
	}
}
