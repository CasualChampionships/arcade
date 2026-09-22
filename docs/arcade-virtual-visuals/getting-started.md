# Virtual Visuals

This module provides utilities for displaying things to players, which includes
bossbars, sidebars, tab displays, cameras, countdowns, and more.

All of these are 'virtual', meaning that they only exist as packets sent to the players
that are shown them. A `VirtualBossbar` is not a boss event that the server knows about,
and a `VirtualSidebar` is not a scoreboard objective. Nothing you display here affects
the server's state, and because of this, two players can be shown completely different
things at the same time.

## Adding to Dependencies

<!--@include: ../joystick.md#usage-->

See [Joystick](../joystick.md) for what the plugin does and its options.
## Where To Start

Every visual in this module is built out of the same two concepts, and it's worth
reading about them first. The [Values Section](values.md) covers how a visual holds
what it displays, and how one player can be shown something different to everyone else.
The [Observing Section](observing.md) then covers how we display a visual to a player,
and how it gets updated.

Once we know those, the individual visuals; [bossbars](bossbars.md),
[sidebars](sidebar.md), and [tab displays](tab-display.md), are all the same ideas
applied to different parts of the screen. The [Elements Section](elements.md) covers
generating what's displayed automatically, instead of setting it ourselves.
