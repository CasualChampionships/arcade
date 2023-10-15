package net.casual.arcade.ducks;

import net.casual.arcade.extensions.Extension;
import net.casual.arcade.extensions.ExtensionHolder;
import net.casual.arcade.extensions.ExtensionMap;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.NotNull;

/**
 * Extension of {@link ExtensionHolder } which is can be
 * implemented using Mixins following the appropriate
 * naming schemes.
 * <p>
 * This is implemented in Java because default methods
 * in Kotlin do not work properly in Java.
 *
 * @see Extension
 * @see ExtensionMap
 */
public interface Arcade$ExtensionHolder extends ExtensionHolder {
	/**
	 * This gets all the extensions that are being held.
	 *
	 * @return The extension map.
	 */
	ExtensionMap arcade$getExtensionMap();

	/**
	 * This gets all the extensions that are being held.
	 *
	 * @return The extension map.
	 */
	@NotNull
	@Override
	@NonExtendable
	default ExtensionMap getExtensionMap() {
		return this.arcade$getExtensionMap();
	}
}
