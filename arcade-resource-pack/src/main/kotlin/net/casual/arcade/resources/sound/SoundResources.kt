package net.casual.arcade.resources.sound

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.casual.arcade.resources.sound.SoundProvider.Type.Event
import net.casual.arcade.resources.sound.SoundProvider.Type.Sound
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent

public abstract class SoundResources(
    public val namespace: String
) {
    private val providers = Object2ObjectLinkedOpenHashMap<String, List<SoundProvider>>()

    protected fun sound(
        location: ResourceLocation,
        volume: Float = 1.0F,
        pitch: Float = 1.0F,
        stream: Boolean = false,
        attenuationDistance: Int = 16,
        dynamicRange: Boolean = false,
        preload: Boolean = false,
        id: String = location.path
    ): SoundEvent {
        val provider = SoundProvider(location, volume, pitch, 1, stream, attenuationDistance, preload, Sound)
        this.providers[id] = listOf(provider)
        return this.register(ResourceLocation.fromNamespaceAndPath(this.namespace, id), attenuationDistance, dynamicRange)
    }

    protected fun event(
        location: ResourceLocation,
        volume: Float = 1.0F,
        pitch: Float = 1.0F,
        stream: Boolean = false,
        attenuationDistance: Int = 16,
        dynamicRange: Boolean = false,
        preload: Boolean = false,
        id: String = location.path
    ): SoundEvent {
        val provider = SoundProvider(location, volume, pitch, 1, stream, attenuationDistance, preload, Event)
        this.providers[id] = listOf(provider)
        return this.register(ResourceLocation.fromNamespaceAndPath(this.namespace, id), attenuationDistance, dynamicRange)
    }

    protected fun group(
        id: String,
        attenuationDistance: Int = 16,
        dynamicRange: Boolean = false,
        builder: GroupedSoundProvider.() -> Unit
    ): SoundEvent {
        val grouped = GroupedSoundProvider()
        grouped.builder()
        this.providers[id] = grouped.getProviders()
        return this.register(ResourceLocation.fromNamespaceAndPath(this.namespace, id), attenuationDistance, dynamicRange)
    }

    protected fun at(path: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(this.namespace, path)
    }

    internal fun toJson(): String {
        val json = buildJsonObject {
            for ((key, providers) in providers) {
                putJsonObject(key) {
                    putJsonArray("sounds") {
                        for (provider in providers) {
                            add(Json.encodeToJsonElement(provider))
                        }
                    }
                }
            }
        }

        return Json.encodeToString(json)
    }

    private fun register(id: ResourceLocation, distance: Int, dynamicRange: Boolean): SoundEvent {
        val sound = if (dynamicRange) {
            SoundEvent.createVariableRangeEvent(id)
        } else {
            SoundEvent.createFixedRangeEvent(id, distance.toFloat())
        }
        return sound
    }
}