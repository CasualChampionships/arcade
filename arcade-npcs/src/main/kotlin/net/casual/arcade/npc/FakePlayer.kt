/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.PropertyMap
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.senseiwells.debug.api.server.DebugToolsPackets
import net.casual.arcade.npc.ai.NPCLookControl
import net.casual.arcade.npc.ai.NPCMoveControl
import net.casual.arcade.npc.mixins.LivingEntityAccessor
import net.casual.arcade.npc.network.FakeConnection
import net.casual.arcade.npc.network.FakeGamePacketListenerImpl
import net.casual.arcade.npc.network.FakeLoginPacketListenerImpl
import net.casual.arcade.npc.pathfinding.navigation.NPCAmphibiousPathNavigation
import net.casual.arcade.npc.pathfinding.navigation.NPCPathNavigation
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.Util
import net.minecraft.core.UUIDUtil
import net.minecraft.network.Connection
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket
import net.minecraft.network.protocol.login.LoginProtocols
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.attributes.DefaultAttributes
import net.minecraft.world.item.ProjectileWeaponItem
import net.minecraft.world.item.component.ResolvableProfile
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.phys.AABB
import java.util.*
import java.util.concurrent.CompletableFuture

@Suppress("LeakingThis")
public open class FakePlayer protected constructor(
    server: MinecraftServer,
    level: ServerLevel,
    profile: GameProfile
): ServerPlayer(server, level, profile, ClientInformation.createDefault()) {
    private val pathfindingMalus = Object2FloatOpenHashMap<PathType>()

    public val moveControl: NPCMoveControl = NPCMoveControl(this)
    public val lookControl: NPCLookControl = NPCLookControl(this)
    public val navigation: NPCPathNavigation = this.createNavigation()

    public open fun createRespawned(
        server: MinecraftServer,
        level: ServerLevel,
        profile: GameProfile
    ): FakePlayer {
        return FakePlayer(server, level, profile)
    }

    public open fun createConnection(
        server: MinecraftServer,
        connection: Connection,
        cookie: CommonListenerCookie
    ): FakeGamePacketListenerImpl {
        return FakeGamePacketListenerImpl(server, connection, this, cookie)
    }

    public open fun connection(): FakeGamePacketListenerImpl {
        return this.connection as FakeGamePacketListenerImpl
    }

    public open fun createAttributeSupplier(): AttributeSupplier {
        return DefaultAttributes.getSupplier(EntityType.PLAYER)
    }

    public open fun createNavigation(): NPCPathNavigation {
        return NPCAmphibiousPathNavigation(this)
    }

    public open fun setPathfindingMalus(type: PathType, float: Float) {
        if (type.malus == float) {
            this.pathfindingMalus.removeFloat(type)
        } else {
            this.pathfindingMalus.put(type, float)
        }
    }

    public open fun getPathfindingMalus(type: PathType): Float {
        return this.pathfindingMalus.getOrDefault(type as Any, type.malus)
    }

    public open fun canFireProjectileWeapon(weapon: ProjectileWeaponItem): Boolean {
        return true
    }

    public open fun isWithinMeleeAttackRange(target: LivingEntity): Boolean {
        return this.getAttackBoundingBox().intersects((target as LivingEntityAccessor).invokeGetHitbox())
    }

    public open fun getAttackBoundingBox(): AABB {
        return this.boundingBox.inflate(this.entityInteractionRange())
    }

    override fun tick() {
        // The player will never send move packets,
        // so we need to manually move the player.
        // This keeps the ticket manager updated
        if (this.server.tickCount % 10 == 0) {
            this.connection.resetPosition()
            this.serverLevel().chunkSource.move(this)
        }
        super.tick()
    }

    override fun serverAiStep() {
        super.serverAiStep()

        this.navigation.tick()

        this.customServerAiStep(this.serverLevel())

        this.moveControl.tick()
        this.lookControl.tick()

        this.isShiftKeyDown = this.moveControl.sneaking

        if (this.shouldStopSprinting()) {
            this.isSprinting = false
        }

        if (this.isUsingItem && !this.isPassenger) {
            this.xxa *= 0.2F
            this.zza *= 0.2F
        }
        if (this.isMovingSlowly()) {
            val sneakingModifier = this.getAttributeValue(Attributes.SNEAKING_SPEED).toFloat()
            this.xxa *= sneakingModifier
            this.zza *= sneakingModifier
        }

        if (this.canStartSprinting() && this.moveControl.sprinting) {
            this.isSprinting = true
        }

        if (this.isSprinting) {
            val shouldStopSprinting = this.zza <= 0 || !this.hasEnoughFoodToStartSprinting()
            val interruptSprinting = shouldStopSprinting || this.horizontalCollision && !this.minorHorizontalCollision
                || this.isInWater && !this.isUnderWater
            if (this.isSwimming) {
                if (!this.onGround() && !this.moveControl.sprinting && shouldStopSprinting || !this.isInWater) {
                    this.isSprinting = false
                }
            } else if (interruptSprinting) {
                this.isSprinting = false
            }
        }

        this.sendDebugPackets()
    }

    public open fun customServerAiStep(level: ServerLevel) {

    }

    override fun tickDeath() {
        this.connection.handleClientCommand(
            ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN)
        )
    }

    override fun forceSetRotation(yRot: Float, xRot: Float) {
        this.yRot = yRot
        this.setYHeadRot(yRot)
        this.xRot = xRot
        this.setOldRot()
    }

    override fun isClientAuthoritative(): Boolean {
        return false
    }

    override fun showEndCredits() {
        this.wonGame = true
        super.showEndCredits()
    }

    protected open fun sendDebugPackets() {
        DebugToolsPackets.getInstance().sendBrainDumpPacket(this.serverLevel(), this)
    }

    private fun isMovingSlowly(): Boolean {
        return this.isCrouching || this.isVisuallyCrawling
    }

    private fun canStartSprinting(): Boolean {
        return !this.isSprinting
            && this.hasEnoughImpulseToStartSprinting()
            && this.hasEnoughFoodToStartSprinting()
            && !this.isUsingItem
            && !this.hasEffect(MobEffects.BLINDNESS)
            && (this.vehicle?.canSprint() ?: true)
            && !this.isFallFlying
            && (!this.isMovingSlowly() || this.isUnderWater)
    }

    private fun shouldStopSprinting(): Boolean {
        return this.isFallFlying
            || this.hasEffect(MobEffects.BLINDNESS)
            || this.isMovingSlowly()
            || this.isPassenger && this.vehicle?.type != EntityType.CAMEL
            || this.isUsingItem && !this.isPassenger && !this.isUnderWater
    }

    private fun hasEnoughImpulseToStartSprinting(): Boolean {
        return if (this.isUnderWater) this.zza > 0 else this.zza >= 0.8
    }

    private fun hasEnoughFoodToStartSprinting(): Boolean {
        return this.isPassenger || this.getFoodData().foodLevel > 6 || this.abilities.mayfly
    }

    public companion object {
        private val joining = Object2ObjectOpenHashMap<String, CompletableFuture<FakePlayer>>()

        public fun join(server: MinecraftServer, profile: GameProfile): CompletableFuture<FakePlayer> {
            return this.join(server, profile, ::FakePlayer)
        }

        public fun <T: FakePlayer> join(
            server: MinecraftServer,
            profile: GameProfile,
            supplier: (MinecraftServer, ServerLevel, GameProfile) -> T
        ): CompletableFuture<T> {
            val connection = FakeConnection()
            // We simulate the fake login packet listener for luckperms compatability
            val login = FakeLoginPacketListenerImpl(server, connection, profile)
            connection.setupInboundProtocol(LoginProtocols.SERVERBOUND, login)
            return login.handleQueries().thenApplyAsync({
                if (server.playerList.getPlayer(profile.id) != null) {
                    throw IllegalArgumentException("Player with UUID ${profile.id} already exists")
                }

                val player = supplier.invoke(server, server.overworld(), profile) as FakePlayer
                player.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, 0x7F)
                server.playerList.placeNewPlayer(
                    connection, player, CommonListenerCookie(profile, 0, player.clientInformation(), false)
                )
                server.connection.connections.add(connection)
                player.connection.handleAcceptPlayerLoad(ServerboundPlayerLoadedPacket())

                // I have no idea why, but we need to downcast to FakePlayer then upcast to T
                // otherwise the kotlin compiler just refuses to compile this valid code???
                @Suppress("UNCHECKED_CAST")
                player as T
            }, server)
        }

        public fun join(server: MinecraftServer, username: String): CompletableFuture<FakePlayer> {
            return this.join(server, username, ::FakePlayer)
        }

        public fun <T: FakePlayer> join(
            server: MinecraftServer,
            username: String,
            supplier: (MinecraftServer, ServerLevel, GameProfile) -> T
        ): CompletableFuture<T> {
            @Suppress("UNCHECKED_CAST")
            return this.joining.getOrPut(username) {
                val resolvable = ResolvableProfile(Optional.of(username), Optional.empty(), PropertyMap())
                resolvable.resolve().whenCompleteAsync({ _, throwable ->
                    this.joining.remove(username)
                    if (throwable != null) {
                        ArcadeUtils.logger.error("Fake player $username failed to join", throwable)
                    }
                }, server).thenCompose { resolved ->
                    val profile = if (resolved.id.get() == Util.NIL_UUID) {
                        GameProfile(UUIDUtil.createOfflinePlayerUUID(username), username)
                    } else {
                        resolved.gameProfile
                    }
                    this.join(server, profile, supplier)
                }
            } as CompletableFuture<T>
        }

        public fun <T: FakePlayer> join(
            server: MinecraftServer,
            uuid: UUID,
            supplier: (MinecraftServer, ServerLevel, GameProfile) -> T
        ): CompletableFuture<T> {
            val resolvable = ResolvableProfile(Optional.empty(), Optional.of(uuid), PropertyMap())
            return resolvable.resolve().thenComposeAsync({ resolved ->
                if (resolved.name.get().isEmpty()) {
                    throw IllegalStateException("Resolved name was empty")
                }
                this.join(server, resolved.gameProfile, supplier)
            }, server)
        }

        public fun isJoining(username: String): Boolean {
            return this.joining.containsKey(username)
        }
    }
}