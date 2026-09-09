package com.tbhelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import org.junit.Test;

public class TroubleBrewingHighlighterConfigTest
{
    private final TroubleBrewingHighlighterConfig config =
        new TroubleBrewingHighlighterConfig()
        {
        };

    @Test
    public void defaultsOnlyUrgentRoutesToFlashing()
    {
        assertTrue(config.flashDamageRepair());
        assertTrue(config.flashConveyor());

        assertFalse(config.flashWater());
        assertFalse(config.flashColouredWater());
        assertFalse(config.flashHoppers());
        assertFalse(config.flashBoilerFuel());
        assertFalse(config.flashBark());
        assertFalse(config.flashProcessedBark());
        assertFalse(config.flashBait());
        assertFalse(config.flashGrubs());
        assertFalse(config.flashFlowers());
        assertFalse(config.flashBitternuts());
        assertFalse(config.flashBitternutsFinal());
        assertFalse(config.flashRum());
    }

    @Test
    public void enablesPassiveHelpersByDefault()
    {
        assertTrue(config.showPiecesOfEight());
        assertTrue(config.showBrewStatus());
        assertTrue(config.showStationAmounts());
        assertTrue(config.showMonkeyDialogueHelper());
        assertTrue(config.preferJoinCrew());
    }

    @Test
    public void defaultsInventoryToItemOutlineAndAfkerModeOff()
    {
        assertEquals(InventoryHighlightStyle.ITEM_OUTLINE, config.inventoryHighlightStyle());
        assertEquals(2, config.inventoryOutlineSize());
        assertFalse(config.afkerMode());
        assertTrue(config.stopWaterHighlightsAt100());
        assertFalse(config.showWaterBucketCount());
    }

    @Test
    public void afkerWaterStopsAtContributionCapUnlessFireIsActive()
    {
        assertTrue(TroubleBrewingHighlighterPlugin.shouldShowAfkerWater(true, 99, false));
        assertFalse(TroubleBrewingHighlighterPlugin.shouldShowAfkerWater(true, 100, false));
        assertTrue(TroubleBrewingHighlighterPlugin.shouldShowAfkerWater(true, 100, true));
        assertTrue(TroubleBrewingHighlighterPlugin.shouldShowAfkerWater(false, 100, false));
    }

    @Test
    public void bucketCountToggleOnlyOverridesNormalBadgesInAfkerMode()
    {
        assertTrue(TroubleBrewingInventoryOverlay.shouldShowSupplyBadge(
            false, true, false, true));
        assertFalse(TroubleBrewingInventoryOverlay.shouldShowSupplyBadge(
            true, true, false, true));
        assertTrue(TroubleBrewingInventoryOverlay.shouldShowSupplyBadge(
            true, true, true, true));
        assertFalse(TroubleBrewingInventoryOverlay.shouldShowSupplyBadge(
            true, false, true, true));
    }

    @Test
    public void defaultsActiveConveyorsToOrange()
    {
        assertEquals(new Color(255, 108, 0), config.conveyorColor());
    }

    @Test
    public void defaultsMonkeyDialogueToBitternutRouteColour()
    {
        assertEquals(config.bitternutColor(), config.monkeyDialogueColor());
    }
}
