# Minigames

Arcade's minigame module provides you almost anything you would need to develop a minigame.

Minigames in arcade are completely sandboxed from the rest of the game allowing you to
run many of the same minigame at the same time. 
With support for all the other arcade modules, you're able to use custom dimensions,
have per minigame custom resource packs, and have customized guis, with all the heavy
lifting done for you!

## Adding to Dependencies

If you are implementing minigames, you probably want to bundle the entirety of arcade
by declaring `modules("arcade")` instead of just this module.

<!--@include: ../joystick.md#usage-->

See [Joystick](../joystick.md) for what the plugin does and its options.
