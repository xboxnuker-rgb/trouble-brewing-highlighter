package com.tbhelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import org.junit.Test;

public class ObjectDatabaseTest
{
    @Test
    public void mapsDistinctBrewingStages()
    {
        assertEquals(
                ResourceType.FLOWERS,
                ObjectDatabase.getObject(ObjectID.BREW_FLOWER_KETTLE)
        );

        assertEquals(
                ResourceType.COLOURED_WATER,
                ObjectDatabase.getObject(ObjectID.BREW_COLOUR_WATER_RED_SIGN)
        );

        assertEquals(
                ResourceType.HOPPERS,
                ObjectDatabase.getObject(ObjectID.BREW_HOPPER_RED)
        );

        assertEquals(
                ResourceType.HOPPERS,
                ObjectDatabase.getObject(ObjectID.BREW_HOPPER_BLUE)
        );

        assertEquals(
                ResourceType.PROCESSED_BARK,
                ObjectDatabase.getObject(ObjectID.BREW_BARK_SIGN)
        );

        // Bottle machine is highlighted only while actively ejecting a bottle.
        assertEquals(
                ResourceType.RUM,
                ObjectDatabase.getObject(ObjectID.GAME_BREW_BOTTLE_MACHINE_SHAKE)
        );

        // Team conveyors are highlighted only while actively carrying rum.
        assertEquals(
                ResourceType.CONVEYOR,
                ObjectDatabase.getObject(ObjectID.BREW_RED_CONVEYOR_1)
        );

        assertEquals(
                ResourceType.CONVEYOR,
                ObjectDatabase.getObject(ObjectID.BREW_BLUE_CONVEYOR_1)
        );

        // Idle machinery is deliberately excluded.
        assertNull(
                ObjectDatabase.getObject(ObjectID.BREW_BOTTLE_MACHINE)
        );

        assertNull(
                ObjectDatabase.getObject(ObjectID.BREW_CONVEYER_BELT)
        );

        // Finished-rum collection crates share the neutral processing route.
        assertEquals(
                ResourceType.HOPPERS,
                ObjectDatabase.getObject(ObjectID.BREW_CRATE_RED)
        );

        assertEquals(
                ResourceType.HOPPERS,
                ObjectDatabase.getObject(ObjectID.BREW_CRATE_BLUE)
        );
    }

    @Test
    public void excludesDepletedSweetgrubMound()
    {
        assertEquals(ResourceType.BAIT, ObjectDatabase.getObject(ObjectID.BREW_SWEETGRUB_MOUND));
        assertNull(ObjectDatabase.getObject(ObjectID.BREW_SWEETGRUB_MOUND_DEPELETED));
    }

    @Test
    public void limitsSwarmSuppressionToTheNearbyMound()
    {
        WorldPoint mound = new WorldPoint(3200, 3200, 0);

        assertTrue(TroubleBrewingHighlighterPlugin.isSwarmNearMound(
            mound, new WorldPoint(3202, 3201, 0)));
        assertFalse(TroubleBrewingHighlighterPlugin.isSwarmNearMound(
            mound, new WorldPoint(3203, 3200, 0)));
        assertFalse(TroubleBrewingHighlighterPlugin.isSwarmNearMound(
            mound, new WorldPoint(3200, 3200, 1)));
    }

    @Test
    public void mapsInventoryStagesSeparately()
    {
        assertEquals(ResourceType.FLOWERS, ObjectDatabase.getItem(ItemID.BOWL_WATER));
        assertEquals(ResourceType.COLOURED_WATER, ObjectDatabase.getItem(ItemID.BREW_BOWL_RED));
        assertEquals(ResourceType.BAIT, ObjectDatabase.getItem(ItemID.RAW_RAT_MEAT));
        assertEquals(ResourceType.GRUBS, ObjectDatabase.getItem(ItemID.BREW_SWEETGRUBS));
        assertEquals(ResourceType.BARK, ObjectDatabase.getItem(ItemID.BREW_SCRAPEY_LOGS));
        assertEquals(ResourceType.BARK, ObjectDatabase.getItem(ItemID.BRONZE_AXE));
        assertEquals(ResourceType.BARK, ObjectDatabase.getItem(ItemID.KNIFE));
        assertEquals(ResourceType.PROCESSED_BARK,
            ObjectDatabase.getItem(ItemID.BREW_SCRAPEY_BARK));
        assertEquals(ResourceType.RUM, ObjectDatabase.getItem(ItemID.BREW_RED_RUM));
    }

    @Test
    public void mapsFireAndRepairStatesSeparately()
    {
        assertEquals(ResourceType.ACTIVE_FIRE,
            ObjectDatabase.getObject(ObjectID.BREW_PIPES_RED_BURNING_1));
        assertEquals(ResourceType.ACTIVE_FIRE,
            ObjectDatabase.getObject(ObjectID.BREW_HOPPER_BLUE_BURNING_2));
        assertEquals(ResourceType.ACTIVE_FIRE,
            ObjectDatabase.getObject(ObjectID.BREW_WATER_PUMP_FIRE));

        assertEquals(ResourceType.PIPE_REPAIR,
            ObjectDatabase.getObject(ObjectID.BREW_PIPES_RED_DAMAGED_1));
        assertEquals(ResourceType.PIPE_REPAIR,
            ObjectDatabase.getObject(ObjectID.BREW_PIPES_BLUE_WET_2));
        assertEquals(ResourceType.PIPE_REPAIR,
            ObjectDatabase.getObject(ObjectID.BREW_WATER_PUMP_DAMAGED));
        assertEquals(ResourceType.PIPE_REPAIR,
            ObjectDatabase.getItem(ItemID.BREW_PIPE_SECTION));

        assertEquals(ResourceType.LUMBER_REPAIR,
            ObjectDatabase.getObject(ObjectID.BREW_HOPPER_RED_DAMAGED_2));
        assertEquals(ResourceType.LUMBER_REPAIR,
            ObjectDatabase.getObject(ObjectID.BREW_HOPPER_BLUE_WET_1));
        assertEquals(ResourceType.LUMBER_REPAIR,
            ObjectDatabase.getItem(ItemID.BREW_LUMBER_PATCH));

        assertEquals(ResourceType.DAMAGE_REPAIR,
            ObjectDatabase.getObject(ObjectID.BREW_BRIDGE_RED_WET_1));
        assertEquals(ResourceType.DAMAGE_REPAIR,
            ObjectDatabase.getObject(ObjectID.BREW_BRIDGE_BLUE_WET_2));
        assertEquals(ResourceType.DAMAGE_REPAIR,
            ObjectDatabase.getItem(ItemID.BREW_BRIDGE_SECTION));
    }

    @Test
    public void interpretsRepairProgressAsPartsRemaining()
    {
        assertEquals(3, TroubleBrewingHighlighterOverlay.repairPartsRemaining(
            ObjectID.BREW_HOPPER_RED_DESTROYED));
        assertEquals(2, TroubleBrewingHighlighterOverlay.repairPartsRemaining(
            ObjectID.BREW_HOPPER_RED_DAMAGED_1));
        assertEquals(1, TroubleBrewingHighlighterOverlay.repairPartsRemaining(
            ObjectID.BREW_HOPPER_RED_DAMAGED_2));
        assertEquals(1, TroubleBrewingHighlighterOverlay.repairPartsRemaining(
            ObjectID.BREW_WATER_PUMP_DAMAGED));
        assertEquals(0, TroubleBrewingHighlighterOverlay.repairPartsRemaining(
            ObjectID.BREW_HOPPER_RED));
    }

    @Test
    public void mapsBoilerStatesSeparately()
    {
        assertEquals(ResourceType.BOILER_EMPTY,
            ObjectDatabase.getObject(ObjectID.BREW_STILL_BOILER));
        assertEquals(ResourceType.BOILER_EMPTY,
            ObjectDatabase.getObject(ObjectID.BREW_STILL_BOILER_CORNER_MIRROR));

        assertEquals(ResourceType.BOILER_UNLIT,
            ObjectDatabase.getObject(ObjectID.BREW_STILL_BOILER_LOGS));
        assertEquals(ResourceType.BOILER_UNLIT,
            ObjectDatabase.getObject(ObjectID.BREW_STILL_BOILER_CORNER_LOGS_MIRROR));

        assertEquals(ResourceType.BOILER_ACTIVE,
            ObjectDatabase.getObject(ObjectID.BREW_STILL_BOILER_FIRE));
        assertEquals(ResourceType.BOILER_ACTIVE,
            ObjectDatabase.getObject(ObjectID.BREW_STILL_BOILER_CORNER_FIRE_MIRROR));

        assertEquals(ResourceType.BOILER_FUEL,
            ObjectDatabase.getObject(ObjectID.BREW_LOG_STORE));
        assertEquals(ResourceType.BOILER_FUEL, ObjectDatabase.getItem(ItemID.LOGS));
        assertEquals(ResourceType.BOILER_FUEL, ObjectDatabase.getItem(ItemID.TINDERBOX));
    }

    @Test
    public void mapsPhysicalBoilersToTeamCounters()
    {
        assertEquals(2, TroubleBrewingHighlighterPlugin.getBoilerCounterIndex(
            ObjectID.BREW_STILL_BOILER_FIRE, false));
        assertEquals(1, TroubleBrewingHighlighterPlugin.getBoilerCounterIndex(
            ObjectID.BREW_STILL_BOILER_CORNER_FIRE, false));
        assertEquals(0, TroubleBrewingHighlighterPlugin.getBoilerCounterIndex(
            ObjectID.BREW_STILL_BOILER_CORNER_MIRROR, false));

        assertEquals(0, TroubleBrewingHighlighterPlugin.getBoilerCounterIndex(
            ObjectID.BREW_STILL_BOILER, true));
        assertEquals(1, TroubleBrewingHighlighterPlugin.getBoilerCounterIndex(
            ObjectID.BREW_STILL_BOILER_CORNER_LOGS, true));
        assertEquals(2, TroubleBrewingHighlighterPlugin.getBoilerCounterIndex(
            ObjectID.BREW_STILL_BOILER_CORNER_FIRE_MIRROR, true));
    }

    @Test
    public void calculatesRemainingRumCapacity()
    {
        assertEquals(29, TroubleBrewingHighlighterPlugin.remainingRums(0));
        assertEquals(22, TroubleBrewingHighlighterPlugin.remainingRums(7));
        assertEquals(0, TroubleBrewingHighlighterPlugin.remainingRums(29));
        assertEquals(0, TroubleBrewingHighlighterPlugin.remainingRums(30));
    }

    @Test
    public void limitsRemainingRumToCyclesThatCanFinish()
    {
        assertPossibleRumsLeft(29, 0, 1200, -1);
        assertPossibleRumsLeft(29, 0, 1140, -1);
        assertPossibleRumsLeft(23, 0, 900, -1);
        assertPossibleRumsLeft(2, 0, 120, -1);
        assertPossibleRumsLeft(3, 0, 120, 10);
        assertPossibleRumsLeft(1, 0, 30, 10);
        assertPossibleRumsLeft(0, 0, 30, -1);
        assertPossibleRumsLeft(0, 0, 5, 0);
        assertPossibleRumsLeft(22, 7, 1200, -1);
        assertPossibleRumsLeft(29, 0, -1, -1);
    }

    @Test
    public void parsesNativeMatchTimerText()
    {
        assertEquals(900, TroubleBrewingHighlighterPlugin.parseMatchSeconds(
            "Time Left: 15 Mins"));
        assertEquals(125, TroubleBrewingHighlighterPlugin.parseMatchSeconds(
            "Time Left: 2:05"));
        assertEquals(-1, TroubleBrewingHighlighterPlugin.parseMatchSeconds("-"));
    }

    @Test
    public void recommendsTheLeastStockedRequiredIngredient()
    {
        assertEquals(
            "Fill sweetgrubs",
            TroubleBrewingHighlighterPlugin.lowestIngredientAction(
                6, 6, 2, 30, 18, 6)
        );
        assertEquals(
            "Fill water buckets",
            TroubleBrewingHighlighterPlugin.lowestIngredientAction(
                6, 4, 6, 5, 18, 6)
        );
        assertNull(TroubleBrewingHighlighterPlugin.lowestIngredientAction(
            6, 6, 6, 30, 18, 6));
        assertNull(TroubleBrewingHighlighterPlugin.lowestIngredientAction(
            0, 0, 0, 0, 0, 0));
    }

    private static void assertPossibleRumsLeft(
        int expected,
        int teamRumMade,
        int matchSecondsRemaining,
        int cycleSecondsRemaining)
    {
        assertEquals(
            expected,
            TroubleBrewingHighlighterPlugin.calculatePossibleRumsLeft(
                teamRumMade,
                matchSecondsRemaining,
                cycleSecondsRemaining
            )
        );
    }

    @Test
    public void mapsMonkeyAndBitternutStagesSeparately()
    {
        // Plain monkey NPC and inventory item match coloured water.
        assertEquals(
                ResourceType.COLOURED_WATER,
                ObjectDatabase.getNpc(NpcID.BREW_MONKEY)
        );
        assertEquals(
                ResourceType.COLOURED_WATER,
                ObjectDatabase.getItem(ItemID.BREW_MONKEY)
        );

        // Coloured monkey inventory items match the bitternut tree.
        assertEquals(
                ResourceType.BITTERNUTS,
                ObjectDatabase.getItem(ItemID.BREW_BLUE_MONKEY)
        );
        assertEquals(
                ResourceType.BITTERNUTS,
                ObjectDatabase.getItem(ItemID.BREW_RED_MONKEY)
        );
        assertEquals(
                ResourceType.BITTERNUTS,
                ObjectDatabase.getObject(ObjectID.BREW_BITTERNUT_TREE)
        );

        // Finished bitternut and its sign use their own final-stage group.
        assertEquals(
                ResourceType.BITTERNUTS_FINAL,
                ObjectDatabase.getItem(ItemID.BREW_BITTERNUT)
        );
        assertEquals(
                ResourceType.BITTERNUTS_FINAL,
                ObjectDatabase.getObject(ObjectID.BREW_BITTERNUT_SIGN)
        );
    }
}
