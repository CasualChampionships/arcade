package net.casual.arcade.resources.sound

import eu.pb4.polymer.core.api.other.PolymerSoundEvent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.casual.arcade.resources.sound.SoundProvider.Type.Event
import net.casual.arcade.resources.sound.SoundProvider.Type.Sound
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation

public abstract class SoundResources(
    public val namespace: String
) {
    private val providers = LinkedHashMap<String, List<SoundProvider>>()

    protected fun sound(
        location: ResourceLocation,
        volume: Float = 1.0F,
        pitch: Float = 1.0F,
        stream: Boolean = false,
        attenuationDistance: Int = 16,
        constantVolume: Boolean = false,
        preload: Boolean = false,
        id: String = location.path
    ): PolymerSoundEvent {
        val provider = SoundProvider(location, volume, pitch, 1, stream, attenuationDistance, preload, Sound)
        this.providers[id] = listOf(provider)
        return this.register(ResourceLocation.fromNamespaceAndPath(this.namespace, id), attenuationDistance, constantVolume)
    }

    protected fun event(
        location: ResourceLocation,
        volume: Float = 1.0F,
        pitch: Float = 1.0F,
        stream: Boolean = false,
        attenuationDistance: Int = 16,
        constantVolume: Boolean = false,
        preload: Boolean = false,
        id: String = location.path
    ): PolymerSoundEvent {
        val provider = SoundProvider(location, volume, pitch, 1, stream, attenuationDistance, preload, Event)
        this.providers[id] = listOf(provider)
        return this.register(ResourceLocation.fromNamespaceAndPath(this.namespace, id), attenuationDistance, constantVolume)
    }

    protected fun group(
        id: String,
        attenuationDistance: Int = 16,
        constantVolume: Boolean = false,
        builder: GroupedSoundProvider.() -> Unit
    ): PolymerSoundEvent {
        val grouped = GroupedSoundProvider()
        grouped.builder()
        this.providers[id] = grouped.getProviders()
        return this.register(ResourceLocation.fromNamespaceAndPath(this.namespace, id), attenuationDistance, constantVolume)
    }

    protected fun at(path: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(this.namespace, path)
    }

    internal fun getJson(): String {
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

    private fun register(id: ResourceLocation, distance: Int, isStatic: Boolean): PolymerSoundEvent {
        val sound = PolymerSoundEvent(null, id, distance.toFloat(), !isStatic, null)
        Registry.register(BuiltInRegistries.SOUND_EVENT, id, sound)
        return sound
    }
}