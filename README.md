# Trouble Brewing Highlighter

A passive RuneLite overlay that colour-codes the resource routes, tools,
ingredients and processing stations used in the Trouble Brewing minigame.
It focuses on finding and following each brewing route while showing cached
Pieces of Eight totals and expected rewards; it does not present match-score
breakdowns or automate any interaction.

## Features

- Highlights relevant world objects and NPCs.
- Highlights relevant inventory items and supplied tools in the tool selector.
- Separates water, flower preparation, coloured water, bark preparation,
  processed bark, hoppers, bait,
  collected sweetgrubs, bitternuts and finished-rum processing.
- Provides independent category visibility, colour and flashing controls.
- Flashes fires/damage and orange active conveyors by default; all other
  flashing is opt-in.
- Provides configurable hulls, tiles, outline width and fill opacity.
- Shows the player's cached Pieces of Eight total near Trouble Brewing in a
  standard overlay panel that can be repositioned with Alt-drag. During a
  match, it also shows the expected new total from capped contribution and the
  team's current rum score.
- Passively highlights Careful (option 3) for the first monkey and Angry
  (option 1) for its paired follow-up. The pair resets after completion or a
  30-second gap, so repeated pairs remain in the correct order. Its default
  outline colour matches the bitternut monkey/tree route.
- Can prioritise the existing Join-crew option on San Fan and Fancy Dan.

The plugin draws passive overlays and can reorder the existing Join-crew menu
entry. It does not create menu actions, inject input, send network requests,
read or write files, or interact with the game automatically.

The Pieces of Eight value is cached rather than polled during rendering. It is
refreshed when the value changes, when the reward shop opens, and when the
end-of-game interface opens. The in-match expected total adds up to 100 points
of personal contribution and 10 points for each bottle of rum on the player's
team score.

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
