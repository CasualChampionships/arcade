/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc

import com.mojang.authlib.GameProfile
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.senseiwells.debug.api.server.DebugToolsPackets
import net.casual.arcade.npc.ai.NPCLookControl
import net.casual.arcade.npc.ai.NPCMoveControl
import net.casual.arcade.npc.configuration.FakePlayerConfigurationTasks
import net.casual.arcade.npc.configuration.FakePlayerConstructor
import net.casual.arcade.npc.mixins.LivingEntityAccessor
import net.casual.arcade.npc.network.FakeConnection
import net.casual.arcade.npc.network.FakeGamePacketListenerImpl
import net.casual.arcade.npc.network.FakeLoginPacketListenerImpl
import net.casual.arcade.npc.pathfinding.navigation.NPCAmphibiousPathNavigation
import net.casual.arcade.npc.pathfinding.navigation.NPCPathNavigation
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.player.DynamicResolvableProfile
import net.casual.arcade.utils.player.server
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
import net.minecraft.world.level.pathfinder.PathType
import net.minecraft.world.phys.AABB
import java.util.*
import java.util.concurrent.CompletableFuture

@Suppress("LeakingThis")
public open class FakePlayer(
    server: MinecraftServer,
    level: ServerLevel,
    profile: GameProfile,
    info: ClientInformation
): ServerPlayer(server, level, profile, info) {
    private val pathfindingMalus = Object2FloatOpenHashMap<PathType>()

    public val moveControl: NPCMoveControl = NPCMoveControl(this)
    public val lookControl: NPCLookControl = NPCLookControl(this)
    public val navigation: NPCPathNavigation = this.createNavigation()

    public open fun createRespawned(
        server: MinecraftServer,
        level: ServerLevel,
        profile: GameProfile,
        info: ClientInformation
    ): FakePlayer {
        return FakePlayer(server, level, profile, info)
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

    public open fun isWithinMeleeAttackRange(
        target: LivingEntity,
        modifier: Double = 0.0
    ): Boolean {
        return this.getAttackBoundingBox(modifier).intersects((target as LivingEntityAccessor).invokeGetHitbox())
    }

    public open fun getAttackBoundingBox(modifier: Double = 0.0): AABB {
        return this.boundingBox.inflate(this.entityInteractionRange() + modifier)
    }

    override fun tick() {
        // The player will never send move packets,
        // so we need to manually move the player.
        // This keeps the ticket manager updated
        if (this.server.tickCount % 10 == 0) {
            this.connection.resetPosition()
            this.level().chunkSource.move(this)
        }
        super.tick()
    }

    override fun serverAiStep() {
        super.serverAiStep()

        this.navigation.tick()

        this.customServerAiStep(this.level())

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
        super.tickDeath()
        this.tryRespawnAfterDeath()
    }

    override fun isClientAuthoritative(): Boolean {
        return false
    }

    override fun showEndCredits() {
        this.wonGame = true
        super.showEndCredits()
    }

    protected open fun tryRespawnAfterDeath() {
        this.connection.handleClientCommand(
            ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN)
        )
    }

    protected open fun sendDebugPackets() {
        DebugToolsPackets.getInstance().sendBrainDumpPacket(this.level(), this)
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
            constructor: FakePlayerConstructor<T>
        ): CompletableFuture<T> {
            val connection = FakeConnection()
            // We simulate the fake login packet listener for luckperms compatability
            val login = FakeLoginPacketListenerImpl(server, connection, profile)
            connection.setupInboundProtocol(LoginProtocols.SERVERBOUND, login)
            return login.handleQueries().thenComposeAsync({
                val cookies = CommonListenerCookie.createInitial(profile, false)
                FakePlayerConfigurationTasks.prepareAndSpawnPlayer(server, profile, connection, cookies, constructor)
            }, server).thenApply { player ->
                player.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, 0x7F)
                server.connection.connections.add(connection)
                player.connection.handleAcceptPlayerLoad(ServerboundPlayerLoadedPacket())
                @Suppress("UNCHECKED_CAST")
                player as T
            }.whenComplete { _, throwable ->
                if (throwable != null) {
                    ArcadeUtils.logger.error("FakePlayer ${profile.name} failed to join", throwable)
                }
            }
        }

        public fun join(server: MinecraftServer, username: String): CompletableFuture<FakePlayer> {
            return this.join(server, username, ::FakePlayer)
        }

        public fun <T: FakePlayer> join(
            server: MinecraftServer,
            username: String,
            constructor: FakePlayerConstructor<T>
        ): CompletableFuture<T> {
            @Suppress("UNCHECKED_CAST")
            return this.joining.getOrPut(username) {
                val resolvable = DynamicResolvableProfile(username)
                resolvable.resolveProfile(server.services().profileResolver).whenCompleteAsync({ _, throwable ->
                    this.joining.remove(username)
                    if (throwable != null) {
                        ArcadeUtils.logger.error("Couldn't resolve FakePlayer username: $username", throwable)
                    }
                }, server).thenCompose { resolved ->
                    this.join(server, resolved, constructor)
                } as CompletableFuture<FakePlayer>
            } as CompletableFuture<T>
        }

        public fun <T: FakePlayer> join(
            server: MinecraftServer,
            uuid: UUID,
            constructor: FakePlayerConstructor<T>
        ): CompletableFuture<T> {
            val resolvable = DynamicResolvableProfile(uuid)
            return resolvable.resolveProfile(server.services().profileResolver).thenComposeAsync({ resolved ->
                if (resolved.name.isEmpty()) {
                    throw IllegalStateException("Resolved name was empty")
                }
                this.join(server, resolved, constructor)
            }, server)
        }

        public fun isJoining(username: String): Boolean {
            return this.joining.containsKey(username)
        }
    }
}