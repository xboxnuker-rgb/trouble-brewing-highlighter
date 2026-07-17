package com.tbhelper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import org.junit.Test;

public class ObjectDatabaseTest
{
    @Test
    public void mapsDistinctBrewingStages()
    {
        assertEquals(ResourceType.FLOWERS, ObjectDatabase.getObject(ObjectID.BREW_FLOWER_KETTLE));
        assertEquals(ResourceType.COLOURED_WATER,
            ObjectDatabase.getObject(ObjectID.BREW_COLOUR_WATER_RED_SIGN));
        assertEquals(ResourceType.HOPPERS, ObjectDatabase.getObject(ObjectID.BREW_HOPPER_RED));
        assertEquals(ResourceType.HOPPERS, ObjectDatabase.getObject(ObjectID.BREW_HOPPER_BLUE));
        assertEquals(ResourceType.PROCESSED_BARK,
            ObjectDatabase.getObject(ObjectID.BREW_BARK_SIGN));
        assertEquals(ResourceType.RUM, ObjectDatabase.getObject(ObjectID.BREW_CONVEYER_BELT));
        assertEquals(ResourceType.RUM, ObjectDatabase.getObject(ObjectID.BREW_CRATE_RED));
    }

    @Test
    public void excludesDepletedSweetgrubMound()
    {
        assertEquals(ResourceType.BAIT, ObjectDatabase.getObject(ObjectID.BREW_SWEETGRUB_MOUND));
        assertNull(ObjectDatabase.getObject(ObjectID.BREW_SWEETGRUB_MOUND_DEPELETED));
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
    public void mapsMonkeysToBitternuts()
    {
        assertEquals(ResourceType.BITTERNUTS, ObjectDatabase.getNpc(NpcID.BREW_MONKEY));
        assertEquals(ResourceType.BITTERNUTS, ObjectDatabase.getNpc(NpcID.BREW_BLUE_MONKEY));
        assertEquals(ResourceType.BITTERNUTS, ObjectDatabase.getNpc(NpcID.BREW_RED_MONKEY));
    }
}
