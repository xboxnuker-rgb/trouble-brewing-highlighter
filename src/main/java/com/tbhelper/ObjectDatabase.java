package com.tbhelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;

/** Trouble Brewing scene objects, items and NPCs verified in-game. */
public final class ObjectDatabase
{
    private static final Map<Integer, ResourceType> OBJECTS;
    private static final Map<Integer, ResourceType> ITEMS;
    private static final Map<Integer, ResourceType> NPCS;

    static
    {
        Map<Integer, ResourceType> objects = new HashMap<>();

        // Resource gathering and preparation.
        register(objects, ObjectID.BREW_WATER_PUMP, ResourceType.WATER);
        register(objects, ObjectID.BREW_FLOWER_KETTLE, ResourceType.FLOWERS);
        register(objects, ObjectID.BREW_SCRAPEY_TREE, ResourceType.BARK);
        register(objects, ObjectID.BREW_SWEETGRUB_MOUND, ResourceType.BAIT);
        // ObjectID.BREW_SWEETGRUB_MOUND_DEPELETED is deliberately excluded.
        register(objects, ObjectID.BREW_BLUE_FLOWERS, ResourceType.FLOWERS);
        register(objects, ObjectID.BREW_RED_FLOWERS, ResourceType.FLOWERS);
        register(objects, ObjectID.BREW_BITTERNUT_TREE, ResourceType.BITTERNUTS);

        // Ingredient picture frames.
        register(objects, ObjectID.BREW_WATER_SIGN, ResourceType.WATER);
        register(objects, ObjectID.BREW_COLOUR_WATER_RED_SIGN, ResourceType.COLOURED_WATER);
        register(objects, ObjectID.BREW_COLOUR_WATER_BLUE_SIGN, ResourceType.COLOURED_WATER);
        register(objects, ObjectID.BREW_BARK_SIGN, ResourceType.PROCESSED_BARK);
        register(objects, ObjectID.BREW_SWEET_GRUBS_SIGN, ResourceType.GRUBS);
        register(objects, ObjectID.BREW_BITTERNUT_SIGN, ResourceType.BITTERNUTS);

        // Brewing stages and finished-rum processing.
        register(objects, ObjectID.BREW_HOPPER_RED, ResourceType.HOPPERS);
        register(objects, ObjectID.BREW_HOPPER_BLUE, ResourceType.HOPPERS);
        register(objects, ObjectID.BREW_BOTTLE_MACHINE, ResourceType.RUM);
        register(objects, ObjectID.BREW_CONVEYER_BELT, ResourceType.RUM);
        register(objects, ObjectID.BREW_CRATE_RED, ResourceType.RUM);
        register(objects, ObjectID.BREW_CRATE_BLUE, ResourceType.RUM);

        OBJECTS = Collections.unmodifiableMap(objects);

        Map<Integer, ResourceType> items = new HashMap<>();

        // Items used on the ground, in the inventory and in the tool selector.
        register(items, ItemID.BUCKET_EMPTY, ResourceType.WATER);
        register(items, ItemID.BREW_BUCKET_DUMMY, ResourceType.WATER);
        register(items, ItemID.BUCKET_WATER, ResourceType.WATER);
        register(items, ItemID.BOWL_EMPTY, ResourceType.FLOWERS);
        register(items, ItemID.BOWL_WATER, ResourceType.FLOWERS);
        register(items, ItemID.BREW_BOWL_RED, ResourceType.COLOURED_WATER);
        register(items, ItemID.BREW_BOWL_BLUE, ResourceType.COLOURED_WATER);
        register(items, ItemID.BREW_SCRAPEY_LOGS, ResourceType.BARK);
        register(items, ItemID.BRONZE_AXE, ResourceType.BARK);
        register(items, ItemID.KNIFE, ResourceType.BARK);
        register(items, ItemID.BREW_SCRAPEY_BARK, ResourceType.PROCESSED_BARK);
        register(items, ItemID.RAW_RAT_MEAT, ResourceType.BAIT);
        register(items, ItemID.BREW_SWEETGRUBS, ResourceType.GRUBS);
        register(items, ItemID.BREW_BLUE_FLOWER, ResourceType.FLOWERS);
        register(items, ItemID.BREW_RED_FLOWER, ResourceType.FLOWERS);
        register(items, ItemID.BREW_BITTERNUT, ResourceType.BITTERNUTS);
        register(items, ItemID.BREW_RED_RUM, ResourceType.RUM);
        register(items, ItemID.BREW_BLUE_RUM, ResourceType.RUM);

        ITEMS = Collections.unmodifiableMap(items);

        Map<Integer, ResourceType> npcs = new HashMap<>();
        register(npcs, NpcID.BREW_MONKEY, ResourceType.BITTERNUTS);
        register(npcs, NpcID.BREW_BLUE_MONKEY, ResourceType.BITTERNUTS);
        register(npcs, NpcID.BREW_RED_MONKEY, ResourceType.BITTERNUTS);
        NPCS = Collections.unmodifiableMap(npcs);
    }

    private ObjectDatabase()
    {
    }

    public static ResourceType getObject(int objectId)
    {
        return OBJECTS.get(objectId);
    }

    public static ResourceType getItem(int itemId)
    {
        return ITEMS.get(itemId);
    }

    public static ResourceType getNpc(int npcId)
    {
        return NPCS.get(npcId);
    }

    private static void register(
        Map<Integer, ResourceType> database,
        int id,
        ResourceType resourceType)
    {
        database.put(id, resourceType);
    }
}
