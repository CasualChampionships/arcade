# Commands

This section of the documentation goes through the different commands that come built-in with Arcade.

## `/minigame`

The minigame command lets you control all aspects of minigames in Arcade.

The first thing to note is that you must be an operator with a permission level of 4 to run this command.

> [!NOTE]
> In the following section anything wrapped in `< >` indicated that it's an argument.

> [!NOTE]
> `<minigame-id>` can be specified by the `uuid` of the minigame, or by the `id` of the minigame (given that there is only one instance), or by `-` which refers to the minigame of the player executing the command.

- `/minigame list` This lists all minigame instances.
- `/minigame create <factory-id>` This allows you to create a minigame instance using a registered minigame factory.
- `/minigame join <minigame-id> <player(s)?>` This allows you to add players to a minigame instance.
- `/minigame leave <player(s)?>` This allows you to remove players from the minigame they are in.


- `/minigame start <minigame-id>` This starts a minigame.
- `/minigame close <minigame-id>` This closes the minigame.
- `/minigame info <minigame-id> <path?>` This gets information about the state of the minigame. The path argument is optional, if not specified, all the info properties will be displayed. You can specify a path if you are only interested in that specific property, e.g. `/minigame info - eliminated_teams`.
- `/minigame team <minigame-id> spectators set <team>` This sets the spectator team which all minigame spectators will join.
- `/minigame team <minigame-id> admins set <team>` This sets the admin team which minigame admins will join.
- `/minigame team <minigame-id> eliminated add <team>` This marks a team as being eliminated.
- `/minigame team <minigame-id> eliminated remove <team>` This un-marks a team as being eliminated.
- `/minigame chat <minigame-id> spies add <player(s)?>` This adds the specified player(s), or the player executing the command if not specified, to be a chat spy. This will make it so this player will see all chat messages (admin, spectator, and team chats). 
- `/minigame chat <minigame-id> spies remove <player(s)?>` This removes the specified player(s) from being a chat spy.
- `/minigame spectating <minigame-id> add <player(s)?>` This marks the player(s) as being a spectator.
- `/minigame spectating <minigame-id> remove <player(s)?>` This un-marks the player(s) as being spectators.
- `/minigame admin <minigame-id> add <player(s)?>` This makes the specified player(s) an admin.
- `/minigame admin <minigame-id> remove <player(s)?>` This removes the specified player(s) from being an admin.
- `/minigame settings <minigame-id>` This opens up the minigame's setting GUI.
- `/minigame settings <minigame-id> <setting>` This gets the value of the specified setting.
- `/minigame settings <minigame-id> <setting> set from option <option>` This sets the value of a setting from one of the pre-defined setting options.
- `/minigame settings <minigame-id> <setting> set from value <value>` This sets the value of a setting to any value specified (this is JSON).
- `/minigame tags <minigame-id> <player> add <tag>` This adds a minigame tag to the specified player. 
- `/minigame tags <minigame-id> <player> remove <tag>` This removes a minigame tag from the specified player.
- `/minigame tags <minigame-id> <player> list` This lists all the minigame tags the specified player has.
- `/minigame phase <minigame-id>` This gets the current phase the minigame is in.
- `/minigame phase <minigame-id> set <phase>` This sets the current phase the minigame is in.
- `/minigame pause <minigame-id>` This pauses the minigame.
- `/minigame unpause <minigame-id>` This unpauses the minigame.
- `/minigame unpause <minigame-id> countdown <time?> <unit?>` This starts a countdown that will unpause the minigame. You can optionally specify a time with a unit, if not specified it will default to 10 seconds.
- `/minigame unpause <minigame-id> ready <players|teams>` This broadcasts a ready check for either all players or teams. Once all are ready, admins will be prompted to run the unpause countdown command.

## `/team`

Arcade makes a small addition to the vanilla `/team` command allowing for randomized teams. 

Teams can be randomly generated in the form `<color> <animal>`. For example, "Red Rhinos", or "Green Geckos".

- `/team randomize with <players> <team-size> <friendly-fire?> <collision?>` This randomly assigns teams to the specified players organizing them into teams of the specified size. You can optionally specify the values for `friendly-fire` and `collision`, if not specified they will default to `false` and `ALWAYS` respectively.
- `/team randomize delete` This will delete any randomly generated teams.

## `/worldborder`

Arcade modifies the world border command to be dimension-specific, as well as providing a way to move the center of the border over a period of time.

- `/worldborder center <pos> <time> <unit>` This linearly interpolates the center of the world border to the new position over the specified time.
- `/worldborder separate` This separates the borders in each dimension, so they will no longer be linked.
- `/worldborder join` This joins the border in each dimension, so moving one will move all of them.