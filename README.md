# Trouble Brewing Highlighter

A passive RuneLite overlay that colour-codes the resource routes, tools,
ingredients and processing stations used in the Trouble Brewing minigame.
It focuses on finding and following each brewing route; it does not track score
or automate any interaction.

## Features

- Highlights relevant world objects, NPCs and ground items.
- Highlights relevant inventory items and supplied tools in the tool selector.
- Separates water, flower preparation, coloured water, bark preparation,
  processed bark, hoppers, bait,
  collected sweetgrubs, bitternuts and finished-rum processing.
- Provides independent category toggles and colours.
- Provides configurable hulls, tiles, outline width and fill opacity.

The plugin only draws overlays. It does not alter menus, inject input, send
network requests, read or write files, or interact with the game.

## Development

Java 11 is required.

Build the plugin:

```powershell
.\gradlew clean build
```

Launch the RuneLite development client:

```powershell
.\gradlew run
```

When using a Jagex Account, follow RuneLite's
[development-client login instructions](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).

The mappings in `ObjectDatabase.java` use RuneLite gameval constants and were
verified in-game with RuneLite Developer Tools. In-game behavior must still be
confirmed manually in both team bases and on both floors after relevant game
updates.

## License

This project is licensed under the BSD 2-Clause License. See `LICENSE`.
