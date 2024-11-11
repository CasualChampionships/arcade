# Commands

This section of the documentation goes through the different commands that come built-in with Arcade.

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