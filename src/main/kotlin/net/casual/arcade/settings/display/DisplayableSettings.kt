package net.casual.arcade.settings.display

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.casual.arcade.settings.GameSetting
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.ScreenUtils
import net.minecraft.world.MenuProvider

public open class DisplayableSettings(
    protected val defaults: DisplayableSettingsDefaults
) {
    private val displays = LinkedHashMap<String, DisplayableGameSetting<*>>()

    /**
     * This registers a setting to this collection.
     *
     * @param display The displayable setting.
     * @return The created [GameSetting].
     */
    public fun <T: Any> register(display: DisplayableGameSetting<T>): GameSetting<T> {
        val setting = display.setting
        this.displays[setting.name] = display
        return setting
    }

    /**
     * This registers a setting to this collection.
     *
     * @param display The displayable settings builder.
     * @return The created [GameSetting].
     */
    public fun <T: Any> register(display: DisplayableGameSettingBuilder<T>): GameSetting<T> {
        return this.register(display.build())
    }

    /**
     * This copies all the settings from another instance
     * of [DisplayableSettings] and registers them here.
     *
     * @param settings The settings to copy from.
     */
    public fun copyFrom(settings: DisplayableSettings) {
        for (setting in settings.displays.values) {
            this.register(setting)
        }
    }

    /**
     * This gets all the registered [GameSetting]s.
     *
     * @return A collection of all the settings.
     */
    public fun all(): Collection<GameSetting<*>> {
        return this.displays.values.map { it.setting }
    }

    /**
     * This gets a setting for a given name.
     *
     * @param name The name of the given setting.
     * @return The setting, may be null if non-existent.
     */
    public fun get(name: String): GameSetting<*>? {
        return this.displays[name]?.setting
    }

    /**
     * This creates a menu which can be displayed to a
     * player to directly interact with the settings.
     *
     * @return The menu provider.
     */
    public open fun menu(parent: MenuProvider? = null): MenuProvider {
        return ScreenUtils.createSettingsMenu(
            this,
            this.defaults.components,
            parent = parent,
            configComponents = this.defaults::createComponents,
        )
    }

    /**
     * This creates a menu which can be displayed to a
     * player to view the settings, without having the
     * ability to modify any of the settings.
     *
     * @return The menu provider.
     */
    public open fun unmodifiableMenu(parent: MenuProvider? = null): MenuProvider {
        return ScreenUtils.createSettingsMenu(
            this,
            this.defaults.components,
            parent,
            configComponents = this.defaults::createComponents,
            modifiable = { false }
        )
    }

    public fun serialize(): JsonArray {
        val settings = JsonArray()
        for (setting in this.all()) {
            val data = JsonObject()
            data.addProperty("name", setting.name)
            data.add("value", setting.serializeValue())
            settings.add(data)
        }
        return settings
    }

    public fun deserialize(settings: JsonArray) {
        for (data in settings.objects()) {
            val name = data.string("name")
            val value = data.get("value")
            val setting = this.get(name)
            if (setting == null || !setting.deserializeAndSetQuietly(value)) {
                continue
            }
        }
    }

    internal fun displays(): Collection<DisplayableGameSetting<*>> {
        return this.displays.values
    }
}