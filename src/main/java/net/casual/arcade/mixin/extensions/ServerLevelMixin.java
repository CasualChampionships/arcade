package net.casual.arcade.mixin.extensions;

import net.casual.arcade.Arcade;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.level.LevelCreatedEvent;
import net.casual.arcade.extensions.ExtensionHolder;
import net.casual.arcade.extensions.ExtensionMap;
import net.casual.arcade.utils.ExtensionUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level implements ExtensionHolder {
	@Unique private final ExtensionMap arcade_extensionMap = new ExtensionMap();
	@Unique private Path arcade_savePath;

	protected ServerLevelMixin(
		WritableLevelData writableLevelData,
		ResourceKey<Level> resourceKey,
		RegistryAccess registryAccess,
		Holder<DimensionType> holder,
		Supplier<ProfilerFiller> supplier,
		boolean bl,
		boolean bl2,
		long l,
		int i
	) {
		super(writableLevelData, resourceKey, registryAccess, holder, supplier, bl, bl2, l, i);
	}

	@Inject(
		method = "<init>",
		at = @At("TAIL")
	)
	private void onCreateLevel(
		MinecraftServer minecraftServer,
		Executor executor,
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		ServerLevelData serverLevelData,
		ResourceKey<Level> resourceKey,
		LevelStem levelStem,
		ChunkProgressListener chunkProgressListener,
		boolean bl,
		long l,
		List<CustomSpawner> list,
		boolean bl2,
		@Nullable RandomSequences randomSequences,
		CallbackInfo ci
	) {
		this.arcade_savePath = levelStorageAccess.getDimensionPath(this.dimension()).resolve("arcade.nbt");

		LevelCreatedEvent event = new LevelCreatedEvent((ServerLevel) (Object) this);
		GlobalEventHandler.broadcast(event);

		try {
			CompoundTag tag = NbtIo.read(this.arcade_savePath.toFile());
			if (tag != null) {
				ExtensionUtils.deserialize(this, tag);
			}
		} catch (IOException e) {
			Arcade.logger.error("Failed to read arcade extension data", e);
		}
	}

	@Inject(
		method = "saveLevelData",
		at = @At("TAIL")
	)
	private void onSaveLevel(CallbackInfo ci) {
		CompoundTag tag = new CompoundTag();
		ExtensionUtils.serialize(this, tag);
		try {
			Files.createDirectories(this.arcade_savePath.getParent());
			NbtIo.write(tag, this.arcade_savePath.toFile());
		} catch (IOException e) {
			Arcade.logger.error("Failed to save arcade extension data", e);
		}
	}

	@Unique
	@NotNull
	@Override
	public ExtensionMap getExtensionMap() {
		return this.arcade_extensionMap;
	}
}
