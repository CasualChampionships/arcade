package net.casual.arcade.util.ducks;

import net.minecraft.network.chat.Component;

public interface CustomMOTD {
	void arcade$setMOTD(Component message);

	Component arcade$getMOTD();
}
