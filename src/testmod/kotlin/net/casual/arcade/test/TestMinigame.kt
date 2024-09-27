package net.casual.arcade.test

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.utils.ResourceUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer

enum class TestPhase(override val id: String): Phase<TestMinigame> {
    First("first"),
    Second("second")
}

class TestMinigame(server: MinecraftServer): Minigame<TestMinigame>(server) {
    override val id: ResourceLocation get() = ID

    override fun getPhases(): Collection<Phase<TestMinigame>> {
        return TestPhase.entries
    }

    companion object {
        val ID = ResourceUtils.arcade("test_minigame")
    }
}