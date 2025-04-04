package net.casual.arcade.npc.ai

import net.casual.arcade.npc.FakePlayer
import net.minecraft.world.entity.ai.control.Control

public class NPCJumpControl(
    private val player: FakePlayer
): Control {
    public var jump: Boolean = false

    public fun jump() {
        this.jump = true
    }

    public fun tick() {
        this.player.setJumping(this.jump)
        this.jump = false
    }
}