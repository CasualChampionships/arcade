package net.casual.arcade.mixin.worldborder;

import net.casual.arcade.ducks.SerializableBorder;
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
		this.setCenter(compound.getDouble("center_x"), compound.getDouble("center_z"));
		this.damagePerBlock = compound.getDouble("damage_per_block");
		this.damageSafeZone = compound.getDouble("damage_safe_zone");
		this.warningBlocks = compound.getInt("warning_blocks");
		this.warningTime = compound.getInt("warning_time");
		long remaining = compound.getLong("lerp_time");
		double size = compound.getDouble("size");
		if (remaining > 0L) {
			this.lerpSizeBetween(size, compound.getDouble("lerp_target"), remaining);
		} else {
			this.setSize(size);
		}
	}
}
