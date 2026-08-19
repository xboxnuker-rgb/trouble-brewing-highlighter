package com.tbhelper;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("trouble-brewing-highlighter")
public interface TroubleBrewingHighlighterConfig extends Config
{
    @ConfigSection(
        name = "Display",
        description = "Overlay appearance",
        position = 0
    )
    String displaySection = "display";

    @ConfigItem(
        position = 0,
        keyName = "outlineWidth",
        name = "Outline Width",
        description = "Thickness of object and item outlines",
        section = displaySection
    )
    @Range(min = 1, max = 8)
    default int outlineWidth()
    {
        return 2;
    }

    @ConfigItem(
        position = 1,
        keyName = "fillOpacity",
        name = "Fill Opacity",
        description = "Opacity of filled object, NPC and item highlights",
        section = displaySection
    )
    @Range(min = 0, max = 255)
    default int fillOpacity()
    {
        return 60;
    }

    @ConfigItem(
        position = 2,
        keyName = "drawHull",
        name = "Draw Convex Hull",
        description = "Draw the model hull of matched scene objects",
        section = displaySection
    )
    default boolean drawHull()
    {
        return true;
    }

    @ConfigItem(
        position = 3,
        keyName = "drawTile",
        name = "Draw Tile",
        description = "Draw tiles beneath matched objects and NPCs",
        section = displaySection
    )
    default boolean drawTile()
    {
        return true;
    }

    @ConfigSection(
        name = "Resources",
        description = "Choose which parts of the brewing route to highlight",
        position = 1
    )
    String resourceSection = "resources";

    @ConfigItem(
        position = 0,
        keyName = "showWater",
        name = "Water & Buckets",
        description = "Highlight water pumps and buckets",
        section = resourceSection
    )
    default boolean showWater()
    {
        return true;
    }

    @ConfigItem(
        position = 1,
        keyName = "showColouredWater",
        name = "Coloured Water",
        description = "Highlight coloured-water bowls and their upstairs signs",
        section = resourceSection
    )
    default boolean showColouredWater()
    {
        return true;
    }

    @ConfigItem(
        position = 2,
        keyName = "showHoppers",
        name = "Team Hoppers",
        description = "Highlight both teams' ingredient hoppers and rum crates",
        section = resourceSection
    )
    default boolean showHoppers()
    {
        return true;
    }

    @ConfigItem(
        position = 3,
        keyName = "showBoilerFuel",
        name = "Boiler Fuel",
        description = "Highlight boiler states, log stores and whichever boiler item is currently needed",
        section = resourceSection
    )
    default boolean showBoilerFuel() { return true; }

    @ConfigItem(
        position = 4,
        keyName = "showBark",
        name = "Bark Preparation",
        description = "Highlight Scrapey trees, logs, knives and the supplied bronze axe",
        section = resourceSection
    )
    default boolean showBark()
    {
        return true;
    }

    @ConfigItem(
        position = 5,
        keyName = "showProcessedBark",
        name = "Processed Scrapey Bark",
        description = "Highlight processed Scrapey bark and its upstairs painting",
        section = resourceSection
    )
    default boolean showProcessedBark()
    {
        return true;
    }

    @ConfigItem(
        position = 6,
        keyName = "showBait",
        name = "Sweetgrub Bait",
        description = "Highlight usable sweetgrub mounds and raw rat meat",
        section = resourceSection
    )
    default boolean showBait()
    {
        return true;
    }

    @ConfigItem(
        position = 7,
        keyName = "showGrubs",
        name = "Collected Sweetgrubs",
        description = "Highlight collected sweetgrubs",
        section = resourceSection
    )
    default boolean showGrubs()
    {
        return true;
    }

    @ConfigItem(
        position = 8,
        keyName = "showFlowers",
        name = "Flowers, Bowls & Kettle",
        description = "Highlight flowers, ordinary bowls and the flower kettle",
        section = resourceSection
    )
    default boolean showFlowers()
    {
        return true;
    }

    @ConfigItem(
        position = 9,
        keyName = "showBitternuts",
        name = "Bitternuts",
        description = "Highlight bitternut trees, monkeys and collected bitternuts",
        section = resourceSection
    )
    default boolean showBitternuts()
    {
        return true;
    }

    @ConfigItem(
        position = 10,
        keyName = "showBitternutsFinal",
        name = "Bitternuts Final",
        description = "Highlight bitternuts and intended hopper",
        section = resourceSection
    )
    default boolean showBitternutsFinal()
    {
        return true;
    }

    @ConfigItem(
            position = 11,
            keyName = "showDamageRepair",
            name = "Damage & Repair",
            description = "Highlight active fires and structure-specific repair states and materials",
            section = resourceSection
    )
    default boolean showDamageRepair()
    {
        return true;
    }

    @ConfigItem(
        position = 12,
        keyName = "showRum",
        name = "Finished Rum",
        description = "Highlight active bottle machines, conveyors and finished rum",
        section = resourceSection
    )
    default boolean showRum()
    {
        return true;
    }

    @ConfigSection(
        name = "Flashing",
        description = "Choose which enabled highlight categories flash",
        position = 2
    )
    String flashingSection = "flashing";

    @ConfigItem(
        position = 0,
        keyName = "flashWater",
        name = "Flash Water & Buckets",
        description = "Make water-route highlights flash",
        section = flashingSection
    )
    default boolean flashWater()
    {
        return false;
    }

    @ConfigItem(
        position = 1,
        keyName = "flashColouredWater",
        name = "Flash Coloured Water",
        description = "Make coloured-water highlights flash",
        section = flashingSection
    )
    default boolean flashColouredWater()
    {
        return false;
    }

    @ConfigItem(
        position = 2,
        keyName = "flashHoppers",
        name = "Flash Team Hoppers",
        description = "Make shared hopper and crate highlights flash",
        section = flashingSection
    )
    default boolean flashHoppers()
    {
        return false;
    }

    @ConfigItem(
        position = 3,
        keyName = "flashBoilerFuel",
        name = "Flash Boiler Fuel",
        description = "Flash team boilers needing logs or lighting and the currently needed boiler item",
        section = flashingSection
    )
    default boolean flashBoilerFuel()
    {
        return false;
    }

    @ConfigItem(
        position = 4,
        keyName = "flashBark",
        name = "Flash Bark Preparation",
        description = "Make bark-preparation highlights flash",
        section = flashingSection
    )
    default boolean flashBark()
    {
        return false;
    }

    @ConfigItem(
        position = 5,
        keyName = "flashProcessedBark",
        name = "Flash Processed Bark",
        description = "Make processed-bark highlights flash",
        section = flashingSection
    )
    default boolean flashProcessedBark()
    {
        return false;
    }

    @ConfigItem(
        position = 6,
        keyName = "flashBait",
        name = "Flash Sweetgrub Bait",
        description = "Make sweetgrub-bait highlights flash",
        section = flashingSection
    )
    default boolean flashBait()
    {
        return false;
    }

    @ConfigItem(
        position = 7,
        keyName = "flashGrubs",
        name = "Flash Collected Sweetgrubs",
        description = "Make collected-sweetgrub highlights flash",
        section = flashingSection
    )
    default boolean flashGrubs()
    {
        return false;
    }

    @ConfigItem(
        position = 8,
        keyName = "flashFlowers",
        name = "Flash Flowers & Kettle",
        description = "Make flower-route highlights flash",
        section = flashingSection
    )
    default boolean flashFlowers()
    {
        return false;
    }

    @ConfigItem(
        position = 9,
        keyName = "flashBitternuts",
        name = "Flash Bitternut Route",
        description = "Make monkey and bitternut-tree highlights flash",
        section = flashingSection
    )
    default boolean flashBitternuts()
    {
        return false;
    }

    @ConfigItem(
        position = 10,
        keyName = "flashBitternutsFinal",
        name = "Flash Finished Bitternuts",
        description = "Make finished-bitternut highlights flash",
        section = flashingSection
    )
    default boolean flashBitternutsFinal()
    {
        return false;
    }

    @ConfigItem(
        position = 11,
        keyName = "flashDamageRepair",
        name = "Flash Fires & Damage",
        description = "Flash your team's active fires and only the repair materials currently needed",
        section = flashingSection
    )
    default boolean flashDamageRepair()
    {
        return true;
    }

    @ConfigItem(
        position = 12,
        keyName = "flashConveyor",
        name = "Flash Active Conveyors",
        description = "Make active rum conveyors flash",
        section = flashingSection
    )
    default boolean flashConveyor()
    {
        return true;
    }

    @ConfigItem(
        position = 13,
        keyName = "flashRum",
        name = "Flash Finished Rum",
        description = "Make finished-rum items and bottle machines flash",
        section = flashingSection
    )
    default boolean flashRum()
    {
        return false;
    }

    @ConfigSection(
        name = "Helpers",
        description = "Passive Trouble Brewing information and convenience helpers",
        position = 3
    )
    String helperSection = "helpers";

    @ConfigItem(
        position = 0,
        keyName = "showPiecesOfEight",
        name = "Show Pieces of Eight",
        description = "Show a movable Pieces of Eight total while near Trouble Brewing",
        section = helperSection
    )
    default boolean showPiecesOfEight()
    {
        return true;
    }

    @ConfigItem(
        position = 1,
        keyName = "showBrewStatus",
        name = "Show Brew Status",
        description = "Show the movable production-guide window during a match",
        section = helperSection
    )
    default boolean showBrewStatus()
    {
        return true;
    }

    @ConfigItem(
        position = 2,
        keyName = "showStationAmounts",
        name = "Show Station Amounts",
        description = "Show ingredient totals and icons at upstairs stations, inventory supply badges and boiler fuel guidance",
        section = helperSection
    )
    default boolean showStationAmounts()
    {
        return true;
    }

    @ConfigItem(
        position = 3,
        keyName = "showMonkeyDialogueHelper",
        name = "Monkey Dialogue Helper",
        description = "Highlight Careful (option 3) first, then Angry (option 1) for paired bitternut monkeys",
        section = helperSection
    )
    default boolean showMonkeyDialogueHelper()
    {
        return true;
    }

    @ConfigItem(
        position = 4,
        keyName = "preferJoinCrew",
        name = "Prioritise Join-crew",
        description = "Make Join-crew the default option on San Fan and Fancy Dan",
        section = helperSection
    )
    default boolean preferJoinCrew()
    {
        return true;
    }

    @ConfigItem(
        position = 5,
        keyName = "monkeyDialogueColor",
        name = "Monkey Dialogue Colour",
        description = "Colour used to outline the recommended monkey dialogue option",
        section = helperSection
    )
    default Color monkeyDialogueColor()
    {
        return new Color(74, 0, 154);
    }

    @ConfigSection(
        name = "Colours (Resetting plugin will restore intended highlights)",
        description = "Highlight colours",
        position = 4
    )
    String colourSection = "colours";

    @ConfigItem(
        position = 0,
        keyName = "waterColor",
        name = "Water",
        description = "Water-route highlight colour",
        section = colourSection
    )
    default Color waterColor()
    {
        return new Color(30, 90, 200);
    }

    @ConfigItem(
        position = 1,
        keyName = "colouredWaterColor",
        name = "Coloured Water",
        description = "Coloured-water route highlight colour",
        section = colourSection
    )
    default Color colouredWaterColor()
    {
        return new Color(187, 70, 255);
    }

    @ConfigItem(
        position = 2,
        keyName = "hopperColor",
        name = "Team Hoppers",
        description = "Both teams' hopper highlight colour",
        section = colourSection
    )
    default Color hopperColor()
    {
        return new Color(0, 71, 69);
    }

    @ConfigItem(
            position = 3,
            keyName = "boilerFuelColour",
            name = "Boiler Fuel",
            description = "Colour used for boiler states, logs, tinderboxes and the log store",
            section = colourSection
    )
    default Color boilerFuelColour() { return new Color(73, 19, 1); }

    @ConfigItem(
        position = 4,
        keyName = "barkColor",
        name = "Bark Preparation",
        description = "Scrapey tree, logs, knife and axe highlight colour",
        section = colourSection
    )
    default Color barkColor()
    {
        return new Color(142, 81, 52);
    }

    @ConfigItem(
        position = 5,
        keyName = "processedBarkColor",
        name = "Processed Scrapey Bark",
        description = "Processed Scrapey bark and painting highlight colour",
        section = colourSection
    )
    default Color processedBarkColor()
    {
        return new Color(255, 105, 180);
    }

    @ConfigItem(
        position = 6,
        keyName = "baitColor",
        name = "Sweetgrub Bait",
        description = "Raw rat meat and usable mound highlight colour",
        section = colourSection
    )
    default Color baitColor()
    {
        return new Color(220, 80, 80);
    }

    @ConfigItem(
        position = 7,
        keyName = "grubColor",
        name = "Sweetgrubs",
        description = "Sweetgrub-route highlight colour",
        section = colourSection
    )
    default Color grubColor()
    {
        return new Color(247, 255, 0);
    }

    @ConfigItem(
        position = 8,
        keyName = "flowerColor",
        name = "Flowers, Bowls & Kettle",
        description = "Flower, ordinary-bowl and kettle highlight colour",
        section = colourSection
    )
    default Color flowerColor()
    {
        return new Color(50, 220, 70);
    }

    @ConfigItem(
        position = 9,
        keyName = "bitternutColor",
        name = "Bitternuts",
        description = "Bitternut highlight colour",
        section = colourSection
    )
    default Color bitternutColor() {
        return new Color(74, 0, 154);
    }

    @ConfigItem(
        position = 10,
        keyName = "bitternutFinalColor",
        name = "Finished Bitternuts",
        description = "Colour used for the collected bitternut and its hopper sign",
        section = colourSection
    )
    default Color bitternutFinalColor()
    {
        return new Color(90, 255, 255);
    }

    @ConfigItem(
        position = 11,
        keyName = "damageRepairColour",
        name = "Fire / Emergency",
        description = "Active-fire, emergency-water and bridge-repair highlight colour",
        section = colourSection
    )
    default Color damageRepairColour()
    {
        return new Color(255, 108, 0);
    }

    @ConfigItem(
        position = 12,
        keyName = "conveyorColor",
        name = "Active Conveyors",
        description = "Active-conveyor highlight colour",
        section = colourSection
    )
    default Color conveyorColor()
    {
        return new Color(255, 108, 0);
    }

    @ConfigItem(
        position = 13,
        keyName = "rumColor",
        name = "Finished Rum",
        description = "Finished-rum highlight colour",
        section = colourSection
    )
    default Color rumColor()
    {
        return new Color(186, 186, 186);
    }
}
