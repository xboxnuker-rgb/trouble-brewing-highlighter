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
        assertTrue(config.showMonkeyDialogueHelper());
        assertTrue(config.preferJoinCrew());
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
