package net.casual.arcade.model

import net.casual.arcade.model.animation.ModelAnimator
import net.casual.arcade.model.animation.pose.BonePose
import net.casual.arcade.model.definition.ModelDefinition
import java.util.UUID

fun ModelDefinition.cubeCount(): Int {
    return this.bones().sumOf { bone -> bone.geometry?.cubes?.count { it.faces.size == 6 } ?: 0 }
}

fun ModelDefinition.poses(): Map<UUID, BonePose> {
    return this.nodes().associate { it.uuid to BonePose() }
}

fun ModelAnimator.tick(times: Int) {
    repeat(times) { this.tick { _, _ -> } }
}

fun BonePose.isCollapsed(): Boolean {
    val scale = this.scale()
    return scale.x < 1.0E-4F || scale.y < 1.0E-4F || scale.z < 1.0E-4F
}