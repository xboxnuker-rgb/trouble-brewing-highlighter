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
        description = "Opacity of filled hulls and ground-item tiles",
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
        description = "Draw tiles beneath matched objects, NPCs and ground items",
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
        description = "Highlight both teams' ingredient hoppers",
        section = resourceSection
    )
    default boolean showHoppers()
    {
        return true;
    }

    @ConfigItem(
        position = 3,
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
        position = 4,
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
        position = 5,
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
        position = 6,
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
        position = 7,
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
        position = 8,
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
        position = 9,
        keyName = "showRum",
        name = "Finished Rum",
        description = "Highlight bottle machines, conveyors, rum crates and finished rum",
        section = resourceSection
    )
    default boolean showRum()
    {
        return true;
    }

    @ConfigSection(
        name = "Colours",
        description = "Highlight colours",
        position = 2
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
        return new Color(170, 70, 255);
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
        return new Color(0, 190, 180);
    }

    @ConfigItem(
        position = 3,
        keyName = "barkColor",
        name = "Bark Preparation",
        description = "Scrapey tree, logs, knife and axe highlight colour",
        section = colourSection
    )
    default Color barkColor()
    {
        return new Color(139, 69, 19);
    }

    @ConfigItem(
        position = 4,
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
        position = 5,
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
        position = 6,
        keyName = "grubColor",
        name = "Sweetgrubs",
        description = "Sweetgrub-route highlight colour",
        section = colourSection
    )
    default Color grubColor()
    {
        return Color.YELLOW;
    }

    @ConfigItem(
        position = 7,
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
        position = 8,
        keyName = "bitternutColor",
        name = "Bitternuts",
        description = "Bitternut highlight colour",
        section = colourSection
    )
    default Color bitternutColor()
    {
        return Color.ORANGE;
    }

    @ConfigItem(
        position = 9,
        keyName = "rumColor",
        name = "Finished Rum",
        description = "Finished-rum highlight colour",
        section = colourSection
    )
    default Color rumColor()
    {
        return Color.LIGHT_GRAY;
    }
}
