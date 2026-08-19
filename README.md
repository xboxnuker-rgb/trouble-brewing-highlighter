# Trouble Brewing Highlighter

A passive RuneLite overlay that colour-codes the resource routes, tools,
ingredients and processing stations used in the Trouble Brewing minigame.
It focuses on finding and following each brewing route while showing cached
Pieces of Eight totals and expected rewards; it does not present match-score
breakdowns or automate any interaction.

Current version: **1.4.1**.

## Version 1.4.1

- Makes the remaining-rum and supply targets time-aware, including active brew
  cycles and a five-second collection/deposit buffer at the end of a match.
- Uses those targets consistently in the Brew Status panel, upstairs station
  labels and inventory badges; inventory monkeys now show the bitternut total
  they help collect.
- Temporarily hides a usable sweetgrub mound highlight while its nearby swarm
  is active and restores it when the swarm despawns.

## Features

- Highlights relevant world objects and NPCs.
- Highlights relevant inventory items and supplied tools in the tool selector.
- Separates water, flower preparation, coloured water, bark preparation,
  processed bark, hoppers, bait,
  collected sweetgrubs, bitternuts and finished-rum processing.
- Hides a usable sweetgrub mound's bait highlight while its nearby swarm is
  active, then restores the highlight when the swarm despawns.
- Provides independent category visibility, colour and flashing controls.
- Shows a visually distinct, Alt-movable Brew Status panel during matches. It
  gives a plain-language next action, colour-coded supply targets for the
  amount of rum that can still finish before the match ends,
  the lowest boiler fuel, current rum-output state, a collection-unlock warning
  when ingredients are needed, a passive cycle estimate, possible rum left and
  the remaining match time. Its next action prioritises the least-stocked
  required ingredient instead of reporting that the system is merely brewing.
  Independently configurable
  station labels show remaining-run supply totals above the player's upstairs
  hopper signs with their matching item icons, and fuel guidance above the
  three team boilers. Loaded, unlit boilers also show a tinderbox icon until
  they are lit.
- Makes boilers context-aware: empty and loaded-but-unlit boilers are tracked
  separately, and inventory logs or a tinderbox are highlighted only while
  that item is useful. Optional boiler flashing affects actionable boilers for
  the player's team; active and opposing-team boilers remain steady.
- Flashes active fires and orange active conveyors by default. A filled water
  bucket switches to the orange emergency colour while the native Trouble
  Brewing HUD reports a fire in the player's team base, including when that
  structure is on the other floor. Bamboo pipes and lumber patches flash only
  while their matching team pipe/water-pump or hopper repair count is present.
  Opposing-team damage remains visible as a steady outline without triggering
  emergency or repair flashing. Local-team fires show a water-bucket prompt;
  extinguished damage shows the correct repair item and one remaining-parts
  label per repair category. The label interprets the spawned progression
  states as `3 → 2 → 1`, then disappears when the repair is complete instead
  of displaying overlapping object-state numbers.
- Uses the water-route colour for damaged pipes and water pumps, and the
  hopper-route colour for damaged hoppers and lumber patches.
- Provides configurable hulls, tiles, outline width and fill opacity.
- Shows the player's cached Pieces of Eight total near Trouble Brewing in a
  standard overlay panel that can be repositioned with Alt-drag. During a
  match, it also shows the expected new total from capped contribution and the
  rum bottles already produced by the player's team.
- Passively highlights Careful (option 3) for the first monkey and Angry
  (option 1) for its paired follow-up. The pair resets after completion or a
  30-second gap, so repeated pairs remain in the correct order. Its default
  outline colour matches the bitternut monkey/tree route.
- Can prioritise the existing Join-crew option on San Fan and Fancy Dan.

The plugin draws passive overlays and can reorder the existing Join-crew menu
entry. It does not create menu actions, inject input, send network requests,
read or write files, or interact with the game automatically.
Ground items are deliberately not highlighted; production supplies are shown
only in the inventory, tool selector and at their relevant world stations.

The Pieces of Eight value is cached rather than polled during rendering. It is
refreshed when the value changes, when the reward shop opens, and when the
end-of-game interface opens. The in-match expected value is derived from the
cached native totals: up to 100 personal-contribution points plus 10 for each
bottle in the player's team rum total.

The Brew Status panel is deliberately phrased for players who do not already
know the minigame. Its supply target starts at the 29-rum theoretical cap, then
uses the native match timer and the estimated 64-tick production cycle to lower
that target as cycles become impossible to finish. A partially completed active
cycle is included using its own remaining time. A fixed five-second end buffer
allows the final rum to be collected and deposited. The result is capped at 29
minus the rum already made, then uses 1 bitternut, 1 sweetgrub, 5 buckets,
3 coloured water and 1 bark per possible rum. Zero stock is red, partial stock
is orange and enough for the time-limited run is green. Supply values, boiler
counts, rum state and timer are cached once per game tick; rendering only
formats those cached values. Its cycle time is an estimate started when the
shared ingredient totals fall.

The movable Brew Status window and the in-world station amounts have separate
toggles. The window can therefore be hidden while retaining the hopper and
boiler guidance. The same setting adds backed, colour-coded current/target
badges to relevant production-supply items in the inventory; tools, repair
materials, boiler fuel and rum remain uncluttered. Inventory monkeys retain
their route colour while their badge shows the team bitternut total and target.

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
