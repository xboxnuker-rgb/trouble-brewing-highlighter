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
        // Bitternut tree - every interaction and team state.
        register(objects, ObjectID.BREW_BITTERNUT_TREE, ResourceType.BITTERNUTS);
        register(objects, ObjectID.BREW_BITTERNUT_TREE_THROW_BLUE, ResourceType.BITTERNUTS);
        register(objects, ObjectID.BREW_BITTERNUT_TREE_THROW_RED, ResourceType.BITTERNUTS);
        register(objects, ObjectID.BREW_BITTERNUT_TREE_CLIMB_BLUE, ResourceType.BITTERNUTS);
        register(objects, ObjectID.BREW_BITTERNUT_TREE_CLIMB_RED, ResourceType.BITTERNUTS);
        register(objects, ObjectID.BREW_BITTERNUT_TREE_READY_BLUE, ResourceType.BITTERNUTS);
        register(objects, ObjectID.BREW_BITTERNUT_TREE_READY_RED, ResourceType.BITTERNUTS);
        register(objects, ObjectID.BREW_BITTERNUT_TREE_FIGHT_BLUE_DEFEND, ResourceType.BITTERNUTS);
        register(objects, ObjectID.BREW_BITTERNUT_TREE_FIGHT_RED_DEFEND, ResourceType.BITTERNUTS);

        // Damage and repair route - only burning, damaged or destroyed states.

        // Red pipes.
        register(objects, ObjectID.BREW_PIPES_RED_BURNING_1, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_PIPES_RED_BURNING_2, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_PIPES_RED_DAMAGED_1, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_PIPES_RED_DAMAGED_2, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_PIPES_RED_DESTROYED, ResourceType.DAMAGE_REPAIR);

        // Red hoppers.
        register(objects, ObjectID.BREW_HOPPER_RED_BURNING_1, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_HOPPER_RED_BURNING_2, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_HOPPER_RED_DAMAGED_1, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_HOPPER_RED_DAMAGED_2, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_HOPPER_RED_DESTROYED, ResourceType.DAMAGE_REPAIR);

        // Red bridges.
        register(objects, ObjectID.BREW_BRIDGE_RED_BURNING_1, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_BRIDGE_RED_BURNING_2, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_BRIDGE_RED_DAMAGED_1, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_BRIDGE_RED_DAMAGED_2, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_BRIDGE_RED_DESTROYED, ResourceType.DAMAGE_REPAIR);

        // Blue pipes.
        register(objects, ObjectID.BREW_PIPES_BLUE_BURNING_1, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_PIPES_BLUE_BURNING_2, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_PIPES_BLUE_DAMAGED_1, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_PIPES_BLUE_DAMAGED_2, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_PIPES_BLUE_DESTROYED, ResourceType.DAMAGE_REPAIR);

        // Blue hoppers.
        register(objects, ObjectID.BREW_HOPPER_BLUE_BURNING_1, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_HOPPER_BLUE_BURNING_2, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_HOPPER_BLUE_DAMAGED_1, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_HOPPER_BLUE_DAMAGED_2, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_HOPPER_BLUE_DESTROYED, ResourceType.DAMAGE_REPAIR);

        // Blue bridges.
        register(objects, ObjectID.BREW_BRIDGE_BLUE_BURNING_1, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_BRIDGE_BLUE_BURNING_2, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_BRIDGE_BLUE_DAMAGED_1, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_BRIDGE_BLUE_DAMAGED_2, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_BRIDGE_BLUE_DESTROYED, ResourceType.DAMAGE_REPAIR);

        // Other damaged production resources.
        register(objects, ObjectID.BREW_WATER_PUMP_FIRE, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_WATER_PUMP_DAMAGED, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_FLOWERS_RED_FIRE, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_FLOWERS_BLUE_FIRE, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_JUNGLE_TREE_1_FIRE, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_JUNGLE_TREE_2_FIRE, ResourceType.DAMAGE_REPAIR);
        register(objects, ObjectID.BREW_JUNGLE_TREE_3_FIRE, ResourceType.DAMAGE_REPAIR);

        // Boiler fuel route.
        register(objects, ObjectID.BREW_STILL_BOILER, ResourceType.BOILER_FUEL);
        register(objects, ObjectID.BREW_STILL_BOILER_MIRROR, ResourceType.BOILER_FUEL);
        register(objects, ObjectID.BREW_STILL_BOILER_FIRE, ResourceType.BOILER_FUEL);
        register(objects, ObjectID.BREW_STILL_BOILER_FIRE_MIRROR, ResourceType.BOILER_FUEL);
        register(objects, ObjectID.BREW_STILL_BOILER_LOGS, ResourceType.BOILER_FUEL);
        register(objects, ObjectID.BREW_STILL_BOILER_LOGS_MIRROR, ResourceType.BOILER_FUEL);
        register(objects, ObjectID.BREW_STILL_BOILER_CORNER, ResourceType.BOILER_FUEL);
        register(objects, ObjectID.BREW_STILL_BOILER_CORNER_FIRE, ResourceType.BOILER_FUEL);
        register(objects, ObjectID.BREW_STILL_BOILER_CORNER_LOGS, ResourceType.BOILER_FUEL);
        register(objects, ObjectID.BREW_STILL_BOILER_CORNER_MIRROR, ResourceType.BOILER_FUEL);
        register(objects, ObjectID.BREW_STILL_BOILER_CORNER_FIRE_MIRROR, ResourceType.BOILER_FUEL);
        register(objects, ObjectID.BREW_STILL_BOILER_CORNER_LOGS_MIRROR, ResourceType.BOILER_FUEL);
        register(objects, ObjectID.BREW_LOG_STORE, ResourceType.BOILER_FUEL);

        // Ingredient picture frames.
        register(objects, ObjectID.BREW_WATER_SIGN, ResourceType.WATER);
        register(objects, ObjectID.BREW_COLOUR_WATER_RED_SIGN, ResourceType.COLOURED_WATER);
        register(objects, ObjectID.BREW_COLOUR_WATER_BLUE_SIGN, ResourceType.COLOURED_WATER);
        register(objects, ObjectID.BREW_BARK_SIGN, ResourceType.PROCESSED_BARK);
        register(objects, ObjectID.BREW_SWEET_GRUBS_SIGN, ResourceType.GRUBS);
        register(objects, ObjectID.BREW_BITTERNUT_SIGN, ResourceType.BITTERNUTS_FINAL);

        // Brewing stages and finished-rum processing.
        register(objects, ObjectID.BREW_HOPPER_RED, ResourceType.HOPPERS);
        register(objects, ObjectID.BREW_HOPPER_BLUE, ResourceType.HOPPERS);

        // Bottle machine only while actively producing a bottle.
        register(objects, ObjectID.GAME_BREW_BOTTLE_MACHINE_SHAKE, ResourceType.RUM);

        // Conveyor only while a bottle is being processed.
        register(objects, ObjectID.BREW_RED_CONVEYOR_1, ResourceType.CONVEYOR);
        register(objects, ObjectID.BREW_BLUE_CONVEYOR_1, ResourceType.CONVEYOR);
        register(objects, ObjectID.BREW_CRATE_RED, ResourceType.HOPPERS);
        register(objects, ObjectID.BREW_CRATE_BLUE, ResourceType.HOPPERS);

        OBJECTS = Collections.unmodifiableMap(objects);

        Map<Integer, ResourceType> items = new HashMap<>();

        // Items shown in the inventory and in the tool selector.
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
        register(items, ItemID.BREW_BITTERNUT, ResourceType.BITTERNUTS_FINAL);
        register(items, ItemID.BREW_RED_RUM, ResourceType.RUM);
        register(items, ItemID.BREW_BLUE_RUM, ResourceType.RUM);
        register(items, ItemID.BREW_MONKEY, ResourceType.COLOURED_WATER);
        register(items, ItemID.BREW_BLUE_MONKEY, ResourceType.BITTERNUTS);
        register(items, ItemID.BREW_RED_MONKEY, ResourceType.BITTERNUTS);

        // Boiler fuel and ignition items.
        register(items, ItemID.LOGS, ResourceType.BOILER_FUEL); // Logs
        register(items, ItemID.TINDERBOX, ResourceType.BOILER_FUEL);  // Tinderbox

        // Repair materials used on damaged structures.
        register(items, ItemID.BREW_PIPE_SECTION, ResourceType.DAMAGE_REPAIR);
        register(items, ItemID.BREW_LUMBER_PATCH, ResourceType.DAMAGE_REPAIR);
        register(items, ItemID.BREW_BRIDGE_SECTION, ResourceType.DAMAGE_REPAIR);

        ITEMS = Collections.unmodifiableMap(items);

        Map<Integer, ResourceType> npcs = new HashMap<>();
        register(npcs, NpcID.BREW_MONKEY, ResourceType.COLOURED_WATER);
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
