package net.casual.arcade.gui.tab

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import org.jetbrains.annotations.ApiStatus.OverrideOnly

public interface PlayerListEntries {
    public val size: Int

    @OverrideOnly
    public fun getEntryAt(index: Int): Entry

    @OverrideOnly
    public fun updateEntries() {

    }

    public data class Entry(
        val display: Component,
        val textures: Texture = Texture.DEFAULT,
        val latency: Int = 0
    ) {
        public companion object {
            public val DEFAULT: Entry = Entry(Component.empty())

            public fun fromPlayer(player: ServerPlayer): Entry {
                return Entry(
                    player.tabListDisplayName ?: PlayerTeam.formatNameForTeam(player.team, player.name),
                    Texture.fromProfile(player.gameProfile) ?: Texture.DEFAULT,
                    player.connection.latency()
                )
            }
        }
    }

    public data class Texture(
        val textures: String,
        val signature: String
    ) {
        public fun toProperty(): Property {
            return Property("textures", this.textures, this.signature)
        }

        public companion object {
            public val DEFAULT: Texture = Texture(
                "ewogICJ0aW1lc3RhbXAiIDogMTcxMTA2MDg2OTYzMywKICAicHJvZmlsZUlkIiA6ICIyNjMxNTcwMzBmZDY0NzAxODc2OWEzNzE4MmZmY2Y0MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJzZW5zZWl3ZWxsczY4IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdiZGEwZjVmMjBmMzQ3OWI0ZDJmZDdhNTQ1NmNiYzZhMjY2NGY3N2ZkNzY3OGFmYjc5ZGVkNjBjNzY0ZWVkM2EiCiAgICB9CiAgfQp9",
                "izhnWfjiGAa3/d0qsUFqopLq8umOS4MY0SEt1zqPQlbXkkFs7tmhA+e+L9ndK0z/wm352mhd6TNEz9nEz8PK+cBeuILvXt59ztW22x00rwa9MKK58Cko+dqjbHNSmn8/Uw/E+9QKS/tsfmQMNjLYQORO6mnIfFUFCm5LCc3Ny+ukleCRhf11UFNImOjk4OOGNZpNMpVVYwtW6cXfQ07/hSf/s+gkOZXBi6rLhW4AS9iKNm+wusTfHx81IgQ3tXL/HvawefEsUUffZMj0o70iro6jy5GC7UfHGmpi7pgSLLhVJ40MfD65/+IFSs4gN6QH2iy7XFGqrGQhhzAllxtvgMb0IsSFWaQy6kHQfm3rElg15LRNmcN3c3aVOWg4FOZamfk9X2zx93QjQO2zPrT7ROjEOVEM4o+bl+kjPkUPWRjf2Q4/7vIubh1fn8cTj6/39euG2EMGWzAsZRJDb5uAMKIlpZ0MGqzmTqIwkGzdkj1MbMDa4gFFFErDHZQ6JtY15WdJ6iFPk9YWXL1pmwTT6d00GLTbFu7hTa6oCaLA6NKH+b/RBD1nCymhrutknXMDHvvQtpvI0A6JXc4tu+FywitdXuKpTXPywdHj9rEhwbb30otMvRfpt1/QNmfEqoIJXGRZomhxNaqgBBHGRkVD6wdUiIrt3MpSsdFGi+Qm0/M="
            )
            public val INVISIBLE: Texture = Texture(
                "ewogICJ0aW1lc3RhbXAiIDogMTY1MTA2OTY1OTc4MCwKICAicHJvZmlsZUlkIiA6ICJjNTZlMjI0MmNiZWY0MWE2ODdlMzI2MGRjMGNmOTM2MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJMSlI3MzEwMCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xY2MwZDhlNDFhNjIzNTYyYmEyOTgxMGI2MjQyOWQ0NDZmYjZkNDkwMzFhZGY1ZDMwYjYzMDFhMDQwOTdmNDI3IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
                "krD08smp1IvxN1EuqdYYGbTMxpkmk+G9sPExcLQ/mX/XPkJQSSa3htEaHqxMTn3J6jb2nhVRGkZ7Gri5gq0IU5JtQE+qa5vg2+wlh1iriUDZixgpidRlnJHiZpFegsUkL6N3fwF9OezKu9IdC/cXaTl22EYQQO4sa1ZvCo81pZsR0dmON8H9HB7doRlx9lWzyAH5wT0mTVmR8hsX7PY3uVxZNyUCKnsHIZftaeUOxBbvzuKlkS5y69BySuvc5PJRPL8lwkaX2c/FihK9Rvee0AwNzhLU/iGixaFO3Zv4erYPUgdsyZa5Zd5gM8Afapzt7mX8eNvPJvcR7yA9ZLOiE/Vt/nPZlRV0mrspc7BlSt2FDz0f72O+QD0Yk8RCxf4czT0wlvfdvt2aCaQZf2EQJUTiN1LT8aOLye8mX47fQ7Lfrywle+G1RzrAZlvXmRt4Dh9Oz/BWQlzVsUg7qi/gGl7nzz8/X3BuWtslx0rlaW6z42jiMzl1JokfYk7v/G2OeGiaw1iBGanDkEMhc4UjNCowX9DvXNPXTLymp+j0KIH+mei7TzNaFUzZlBfkOPWvR4iSR61ycWmFqfXOV+6xskDXXqV9WDZuuLFzZmQ3Mjnw30sJoQ+sMcV1OcSxxxrTHDnFoy+uMlq9VW5pGwdvOR/nmU6uOfQ0geeCVYME+5w="
            )

            public fun fromProfile(profile: GameProfile): Texture? {
                val property = profile.properties.get("textures").firstOrNull() ?: return null
                return Texture(
                    property.value,
                    property.signature ?: return null
                )
            }
        }
    }
}