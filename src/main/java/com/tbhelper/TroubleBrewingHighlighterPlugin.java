package com.tbhelper;

import com.google.inject.Provides;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.DecorativeObject;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.GroundObject;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.WallObject;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.DecorativeObjectDespawned;
import net.runelite.api.events.DecorativeObjectSpawned;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GroundObjectDespawned;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@PluginDescriptor(
        name = "Trouble Brewing Highlighter",
        description = "Colour-coded highlights for Trouble Brewing resources and processing stations",
        tags = {"trouble", "brewing", "minigame", "highlight", "helper"}
)
public class TroubleBrewingHighlighterPlugin extends Plugin
{
    private static final int BOOTSTRAP_SCAN_TICKS = 20;
    private static final int TROUBLE_BREWING_REGION_ID = 15150;
    private static final int TEAM_BASE_DIVIDE_Y = 2975;
    private static final int BREW_CYCLE_TICKS = 64;
    private static final int BREW_CYCLE_GRACE_TICKS = 50;
    private static final int MAX_BOILER_LOGS = 10;
    private static final int MAX_RUM_PER_GAME = 29;
    private static final int RUM_COLLECTION_CUSHION_SECONDS = 5;
    private static final int RUM_READY_STATE = 2;
    private static final int SWARM_MOUND_DISTANCE = 2;
    private static final Pattern MATCH_TIME_PATTERN = Pattern.compile(
        "(\\d+)(?::(\\d{1,2}))?"
    );
    private static final long MONKEY_PAIR_WINDOW_MS = 30_000L;
    private static final String MONKEY_CAREFUL_KEYWORD = "careful";
    private static final String MONKEY_ANGRY_KEYWORD = "angry";
    private static final int BREW_GAME_OVER_GROUP_ID = WidgetUtil.componentToInterface(
        InterfaceID.BrewGameOver.BREW_BACKING_SCROLL
    );
    private static final int SHOP_GROUP_ID = WidgetUtil.componentToInterface(
        InterfaceID.Shopmain.UNIVERSE
    );

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private TroubleBrewingHighlighterOverlay overlay;

    @Inject
    private TroubleBrewingInventoryOverlay inventoryOverlay;

    @Inject
    private PiecesOfEightOverlay piecesOfEightOverlay;

    @Inject
    private BrewStatusOverlay brewStatusOverlay;

    @Inject
    private MonkeyDialogueOverlay monkeyDialogueOverlay;

    @Inject
    private TroubleBrewingHighlighterConfig config;

    private final Map<TileObject, HighlightedObject> highlightedObjects = new IdentityHashMap<>();
    private final Map<ResourceType, Integer> redTeamObjectCounts =
        new EnumMap<>(ResourceType.class);
    private final Map<ResourceType, Integer> blueTeamObjectCounts =
        new EnumMap<>(ResourceType.class);
    private final Map<NPC, HighlightedNpc> highlightedNpcs = new IdentityHashMap<>();
    private final Set<NPC> vicinityNpcs = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<NPC> swarmNpcs = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<TileObject> swarmedSweetgrubMounds =
        Collections.newSetFromMap(new IdentityHashMap<>());
    private int bootstrapTicksRemaining;
    private int piecesOfEight;
    private int expectedPiecesOfEight;
    private int teamBitternuts;
    private int teamSweetgrubs;
    private int teamBuckets;
    private int teamColouredWater;
    private int teamBark;
    private int previousTeamBitternuts = -1;
    private int previousTeamSweetgrubs = -1;
    private int previousTeamBuckets = -1;
    private int previousTeamColouredWater = -1;
    private int previousTeamBark = -1;
    private int boiler1Logs;
    private int boiler2Logs;
    private int boiler3Logs;
    private int teamHopperRepairParts;
    private int teamPipeRepairParts;
    private int teamBridgeRepairParts;
    private int teamRumMade;
    private int rumLoadsAvailable;
    private int possibleRumsLeft;
    private int brewCycleEndTick = -1;
    private boolean teamHopperOnFire;
    private boolean teamPipesOnFire;
    private boolean teamBridgeOnFire;
    private boolean rumReady;
    private String matchTime = "-";
    private MonkeyAdvice nextMonkeyAdvice = MonkeyAdvice.CAREFUL;
    private MonkeyAdvice activeMonkeyAdvice = MonkeyAdvice.CAREFUL;
    private boolean monkeyAdviceMenuVisible;
    private long carefulAdviceClosedAt;

    @Override
    protected void startUp()
    {
        overlayManager.add(overlay);
        overlayManager.add(inventoryOverlay);
        overlayManager.add(piecesOfEightOverlay);
        overlayManager.add(brewStatusOverlay);
        overlayManager.add(monkeyDialogueOverlay);
        beginBootstrapScan();
        clientThread.invokeLater(this::updatePiecesOfEight);
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        overlayManager.remove(inventoryOverlay);
        overlayManager.remove(piecesOfEightOverlay);
        overlayManager.remove(brewStatusOverlay);
        overlayManager.remove(monkeyDialogueOverlay);
        bootstrapTicksRemaining = 0;
        piecesOfEight = 0;
        expectedPiecesOfEight = 0;
        resetBrewStatus();
        resetMonkeyAdviceCycle();
        clearCache();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        GameState gameState = event.getGameState();

        if (gameState == GameState.LOGGED_IN)
        {
            beginBootstrapScan();
            updatePiecesOfEight();
        }
        else
        {
            bootstrapTicksRemaining = 0;
            resetBrewStatus();
            resetMonkeyAdviceCycle();
            clearCache();
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        updateExpectedPiecesOfEight();
        updateBrewStatus();
        updateMonkeyAdviceState();

        if (bootstrapTicksRemaining <= 0)
        {
            return;
        }

        rebuildSceneCache();
        bootstrapTicksRemaining--;
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event)
    {
        trackObject(event.getGameObject());
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event)
    {
        untrackObject(event.getGameObject());
    }

    @Subscribe
    public void onWallObjectSpawned(WallObjectSpawned event)
    {
        trackObject(event.getWallObject());
    }

    @Subscribe
    public void onWallObjectDespawned(WallObjectDespawned event)
    {
        untrackObject(event.getWallObject());
    }

    @Subscribe
    public void onDecorativeObjectSpawned(DecorativeObjectSpawned event)
    {
        trackObject(event.getDecorativeObject());
    }

    @Subscribe
    public void onDecorativeObjectDespawned(DecorativeObjectDespawned event)
    {
        untrackObject(event.getDecorativeObject());
    }

    @Subscribe
    public void onGroundObjectSpawned(GroundObjectSpawned event)
    {
        trackObject(event.getGroundObject());
    }

    @Subscribe
    public void onGroundObjectDespawned(GroundObjectDespawned event)
    {
        untrackObject(event.getGroundObject());
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned event)
    {
        trackNpc(event.getNpc());
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned event)
    {
        highlightedNpcs.remove(event.getNpc());
        vicinityNpcs.remove(event.getNpc());
        if (swarmNpcs.remove(event.getNpc()))
        {
            refreshSwarmedSweetgrubMounds();
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event)
    {
        if (event.getVarpId() == VarPlayerID.BREW_PIECES)
        {
            updatePiecesOfEight();
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event)
    {
        int groupId = event.getGroupId();
        if (groupId == SHOP_GROUP_ID || groupId == BREW_GAME_OVER_GROUP_ID)
        {
            updatePiecesOfEight();
        }
    }

    @Subscribe
    public void onPostMenuSort(PostMenuSort event)
    {
        if (!config.preferJoinCrew() || client.isMenuOpen())
        {
            return;
        }

        MenuEntry[] menuEntries = client.getMenuEntries();
        int joinCrewIndex = -1;

        for (int i = menuEntries.length - 1; i >= 0; i--)
        {
            MenuEntry menuEntry = menuEntries[i];
            NPC npc = menuEntry.getNpc();
            if (npc != null
                && isTeamAttendant(npc.getId())
                && isJoinCrewOption(menuEntry.getOption()))
            {
                joinCrewIndex = i;
                break;
            }
        }

        int defaultIndex = menuEntries.length - 1;
        if (joinCrewIndex >= 0 && joinCrewIndex != defaultIndex)
        {
            MenuEntry defaultEntry = menuEntries[defaultIndex];
            menuEntries[defaultIndex] = menuEntries[joinCrewIndex];
            menuEntries[joinCrewIndex] = defaultEntry;
            client.setMenuEntries(menuEntries);
        }
    }

    private void beginBootstrapScan()
    {
        bootstrapTicksRemaining = BOOTSTRAP_SCAN_TICKS;
        clientThread.invokeLater(this::rebuildSceneCache);
    }

    private void rebuildSceneCache()
    {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            return;
        }

        Set<WorldView> visited = Collections.newSetFromMap(new IdentityHashMap<>());

        // Trouble Brewing is instanced. Scan both the top-level view and the
        // player's active sub-worldview, then recurse through any child views.
        rebuildWorldView(client.getTopLevelWorldView(), visited);

        Player localPlayer = client.getLocalPlayer();
        if (localPlayer != null)
        {
            rebuildWorldView(localPlayer.getWorldView(), visited);
        }
    }

    private void rebuildWorldView(WorldView worldView, Set<WorldView> visited)
    {
        if (worldView == null || !visited.add(worldView))
        {
            return;
        }

        for (NPC npc : worldView.npcs())
        {
            if (npc != null)
            {
                trackNpc(npc);
            }
        }

        Scene scene = worldView.getScene();
        Tile[][][] tiles = scene == null ? null : scene.getTiles();
        if (tiles != null)
        {
            // Cache every loaded plane once so highlights remain available when the
            // player moves between the ground and upper floors of either base.
            for (Tile[][] planeTiles : tiles)
            {
                for (Tile[] column : planeTiles)
                {
                    for (Tile tile : column)
                    {
                        if (tile != null)
                        {
                            scanTile(tile);
                        }
                    }
                }
            }
        }

        for (WorldView child : worldView.worldViews())
        {
            rebuildWorldView(child, visited);
        }
    }

    private void scanTile(Tile tile)
    {
        for (GameObject object : tile.getGameObjects())
        {
            if (object != null)
            {
                trackObject(object);
            }
        }

        WallObject wallObject = tile.getWallObject();
        if (wallObject != null)
        {
            trackObject(wallObject);
        }

        DecorativeObject decorativeObject = tile.getDecorativeObject();
        if (decorativeObject != null)
        {
            trackObject(decorativeObject);
        }

        GroundObject groundObject = tile.getGroundObject();
        if (groundObject != null)
        {
            trackObject(groundObject);
        }

    }

    private void trackObject(TileObject object)
    {
        ResourceType resourceType = ObjectDatabase.getObject(object.getId());
        if (resourceType == null)
        {
            untrackObject(object);
            return;
        }

        HighlightedObject previous = highlightedObjects.put(
                object,
                new HighlightedObject(object, resourceType, getObjectTeam(object))
        );
        HighlightedObject current = highlightedObjects.get(object);

        if (previous == null)
        {
            incrementObjectCount(current);
        }
        else if (previous.getResourceType() != resourceType
            || previous.getTeam() != current.getTeam())
        {
            decrementObjectCount(previous);
            incrementObjectCount(current);
        }

        updateSwarmedSweetgrubMound(current);
    }

    private void untrackObject(TileObject object)
    {
        HighlightedObject removed = highlightedObjects.remove(object);
        if (removed != null)
        {
            decrementObjectCount(removed);
        }
        swarmedSweetgrubMounds.remove(object);
    }

    private void incrementObjectCount(HighlightedObject highlightedObject)
    {
        if (highlightedObject.getTeam() == BrewTeam.NONE)
        {
            return;
        }

        ResourceType resourceType = highlightedObject.getResourceType();
        teamCounts(highlightedObject.getTeam()).merge(resourceType, 1, Integer::sum);
    }

    private void decrementObjectCount(HighlightedObject highlightedObject)
    {
        if (highlightedObject.getTeam() == BrewTeam.NONE)
        {
            return;
        }

        ResourceType resourceType = highlightedObject.getResourceType();
        Map<ResourceType, Integer> teamCounts = teamCounts(highlightedObject.getTeam());
        int teamCount = teamCounts.getOrDefault(resourceType, 0);
        if (teamCount <= 1)
        {
            teamCounts.remove(resourceType);
        }
        else
        {
            teamCounts.put(resourceType, teamCount - 1);
        }
    }

    private Map<ResourceType, Integer> teamCounts(BrewTeam team)
    {
        return team == BrewTeam.BLUE ? blueTeamObjectCounts : redTeamObjectCounts;
    }

    private static BrewTeam getObjectTeam(TileObject object)
    {
        BrewTeam explicitTeam = getExplicitObjectTeam(object.getId());
        if (explicitTeam != BrewTeam.NONE)
        {
            return explicitTeam;
        }

        int y = object.getWorldLocation().getY();
        if (y > TEAM_BASE_DIVIDE_Y)
        {
            return BrewTeam.RED;
        }

        return y < TEAM_BASE_DIVIDE_Y ? BrewTeam.BLUE : BrewTeam.NONE;
    }

    private static BrewTeam getExplicitObjectTeam(int objectId)
    {
        switch (objectId)
        {
            case ObjectID.BREW_PIPES_RED_BURNING_1:
            case ObjectID.BREW_PIPES_RED_BURNING_2:
            case ObjectID.BREW_PIPES_RED_DAMAGED_1:
            case ObjectID.BREW_PIPES_RED_DAMAGED_2:
            case ObjectID.BREW_PIPES_RED_WET_1:
            case ObjectID.BREW_PIPES_RED_WET_2:
            case ObjectID.BREW_PIPES_RED_DESTROYED:
            case ObjectID.BREW_HOPPER_RED_BURNING_1:
            case ObjectID.BREW_HOPPER_RED_BURNING_2:
            case ObjectID.BREW_HOPPER_RED_DAMAGED_1:
            case ObjectID.BREW_HOPPER_RED_DAMAGED_2:
            case ObjectID.BREW_HOPPER_RED_WET_1:
            case ObjectID.BREW_HOPPER_RED_WET_2:
            case ObjectID.BREW_HOPPER_RED_DESTROYED:
            case ObjectID.BREW_BRIDGE_RED_BURNING_1:
            case ObjectID.BREW_BRIDGE_RED_BURNING_2:
            case ObjectID.BREW_BRIDGE_RED_DAMAGED_1:
            case ObjectID.BREW_BRIDGE_RED_DAMAGED_2:
            case ObjectID.BREW_BRIDGE_RED_WET_1:
            case ObjectID.BREW_BRIDGE_RED_WET_2:
            case ObjectID.BREW_BRIDGE_RED_DESTROYED:
            case ObjectID.BREW_FLOWERS_RED_FIRE:
                return BrewTeam.RED;
            case ObjectID.BREW_PIPES_BLUE_BURNING_1:
            case ObjectID.BREW_PIPES_BLUE_BURNING_2:
            case ObjectID.BREW_PIPES_BLUE_DAMAGED_1:
            case ObjectID.BREW_PIPES_BLUE_DAMAGED_2:
            case ObjectID.BREW_PIPES_BLUE_WET_1:
            case ObjectID.BREW_PIPES_BLUE_WET_2:
            case ObjectID.BREW_PIPES_BLUE_DESTROYED:
            case ObjectID.BREW_HOPPER_BLUE_BURNING_1:
            case ObjectID.BREW_HOPPER_BLUE_BURNING_2:
            case ObjectID.BREW_HOPPER_BLUE_DAMAGED_1:
            case ObjectID.BREW_HOPPER_BLUE_DAMAGED_2:
            case ObjectID.BREW_HOPPER_BLUE_WET_1:
            case ObjectID.BREW_HOPPER_BLUE_WET_2:
            case ObjectID.BREW_HOPPER_BLUE_DESTROYED:
            case ObjectID.BREW_BRIDGE_BLUE_BURNING_1:
            case ObjectID.BREW_BRIDGE_BLUE_BURNING_2:
            case ObjectID.BREW_BRIDGE_BLUE_DAMAGED_1:
            case ObjectID.BREW_BRIDGE_BLUE_DAMAGED_2:
            case ObjectID.BREW_BRIDGE_BLUE_WET_1:
            case ObjectID.BREW_BRIDGE_BLUE_WET_2:
            case ObjectID.BREW_BRIDGE_BLUE_DESTROYED:
            case ObjectID.BREW_FLOWERS_BLUE_FIRE:
                return BrewTeam.BLUE;
            default:
                return BrewTeam.NONE;
        }
    }

    private void trackNpc(NPC npc)
    {
        if (npc.getId() == NpcID.BREW_SWARM && swarmNpcs.add(npc))
        {
            refreshSwarmedSweetgrubMounds();
        }

        if (isVicinityNpc(npc.getId()))
        {
            vicinityNpcs.add(npc);
        }

        ResourceType resourceType = ObjectDatabase.getNpc(npc.getId());
        if (resourceType != null)
        {
            highlightedNpcs.put(
                    npc,
                    new HighlightedNpc(npc, resourceType)
            );
        }
    }

    private void refreshSwarmedSweetgrubMounds()
    {
        swarmedSweetgrubMounds.clear();
        for (HighlightedObject highlightedObject : highlightedObjects.values())
        {
            updateSwarmedSweetgrubMound(highlightedObject);
        }
    }

    private void updateSwarmedSweetgrubMound(HighlightedObject highlightedObject)
    {
        TileObject tileObject = highlightedObject.getTileObject();
        if (highlightedObject.getResourceType() != ResourceType.BAIT)
        {
            swarmedSweetgrubMounds.remove(tileObject);
            return;
        }

        WorldPoint moundLocation = tileObject.getWorldLocation();
        for (NPC swarm : swarmNpcs)
        {
            if (isSwarmNearMound(moundLocation, swarm.getWorldLocation()))
            {
                swarmedSweetgrubMounds.add(tileObject);
                return;
            }
        }

        swarmedSweetgrubMounds.remove(tileObject);
    }

    static boolean isSwarmNearMound(WorldPoint moundLocation, WorldPoint swarmLocation)
    {
        return moundLocation != null
            && swarmLocation != null
            && moundLocation.distanceTo(swarmLocation) <= SWARM_MOUND_DISTANCE;
    }

    private void clearCache()
    {
        highlightedObjects.clear();
        redTeamObjectCounts.clear();
        blueTeamObjectCounts.clear();
        highlightedNpcs.clear();
        vicinityNpcs.clear();
        swarmNpcs.clear();
        swarmedSweetgrubMounds.clear();
    }

    private void updatePiecesOfEight()
    {
        if (client.getGameState() == GameState.LOGGED_IN)
        {
            piecesOfEight = client.getVarpValue(VarPlayerID.BREW_PIECES);
            if (!isTroubleBrewingMatchActive())
            {
                expectedPiecesOfEight = piecesOfEight;
            }
        }
    }

    private void updateExpectedPiecesOfEight()
    {
        if (!isTroubleBrewingMatchActive())
        {
            expectedPiecesOfEight = piecesOfEight;
            return;
        }

        int contribution = Math.min(
            Math.max(client.getVarbitValue(VarbitID.BREW_PLAYER_REWARD), 0),
            100
        );
        Widget teamScoreWidget = client.getWidget(
            isBlueTeam() ? InterfaceID.BrewOverlay.BLUE_SCORE : InterfaceID.BrewOverlay.RED_SCORE
        );
        teamRumMade = parseNonNegativeInteger(teamScoreWidget);
        expectedPiecesOfEight = piecesOfEight + contribution + (teamRumMade * 10);
    }

    private void updateBrewStatus()
    {
        if (!isTroubleBrewingMatchActive())
        {
            resetBrewStatus();
            return;
        }

        int bitternuts = parseNonNegativeInteger(
            client.getWidget(InterfaceID.BrewOverlay.BITTERNUT_COUNT)
        );
        int sweetgrubs = parseNonNegativeInteger(
            client.getWidget(InterfaceID.BrewOverlay.SWEETGRUB_COUNT)
        );
        int buckets = parseNonNegativeInteger(
            client.getWidget(InterfaceID.BrewOverlay.BUCKET_COUNT)
        );
        int colouredWater = parseNonNegativeInteger(
            client.getWidget(InterfaceID.BrewOverlay.COLOURWATER_COUNT)
        );
        int bark = parseNonNegativeInteger(
            client.getWidget(InterfaceID.BrewOverlay.BARK_COUNT)
        );

        boolean batchStarted = decreased(previousTeamBitternuts, bitternuts)
            || decreased(previousTeamSweetgrubs, sweetgrubs)
            || decreased(previousTeamBuckets, buckets)
            || decreased(previousTeamColouredWater, colouredWater)
            || decreased(previousTeamBark, bark);
        if (batchStarted)
        {
            brewCycleEndTick = client.getTickCount() + BREW_CYCLE_TICKS;
        }
        else if (brewCycleEndTick >= 0
            && client.getTickCount() > brewCycleEndTick + BREW_CYCLE_GRACE_TICKS)
        {
            brewCycleEndTick = -1;
        }

        teamBitternuts = bitternuts;
        teamSweetgrubs = sweetgrubs;
        teamBuckets = buckets;
        teamColouredWater = colouredWater;
        teamBark = bark;
        previousTeamBitternuts = bitternuts;
        previousTeamSweetgrubs = sweetgrubs;
        previousTeamBuckets = buckets;
        previousTeamColouredWater = colouredWater;
        previousTeamBark = bark;

        boiler1Logs = parseNonNegativeInteger(
            client.getWidget(InterfaceID.BrewOverlay.BOILER1_COUNT)
        );
        boiler2Logs = parseNonNegativeInteger(
            client.getWidget(InterfaceID.BrewOverlay.BOILER2_COUNT)
        );
        boiler3Logs = parseNonNegativeInteger(
            client.getWidget(InterfaceID.BrewOverlay.BOILER3_COUNT)
        );
        teamHopperRepairParts = parseNonNegativeInteger(
            client.getWidget(InterfaceID.BrewOverlay.HOPPER_AMOUNT)
        );
        teamPipeRepairParts = parseNonNegativeInteger(
            client.getWidget(InterfaceID.BrewOverlay.PIPE_AMOUNT)
        );
        teamBridgeRepairParts = parseNonNegativeInteger(
            client.getWidget(InterfaceID.BrewOverlay.BRIDGE_AMOUNT)
        );
        teamHopperOnFire = isVisible(InterfaceID.BrewOverlay.HOPPER_FIRE);
        teamPipesOnFire = isVisible(InterfaceID.BrewOverlay.PIPE_FIRE);
        teamBridgeOnFire = isVisible(InterfaceID.BrewOverlay.BRIDGE_FIRE);
        rumLoadsAvailable = Math.max(
            client.getVarbitValue(VarbitID.BREW_LOADS_AVAILABLE),
            0
        );
        int rumState = client.getVarbitValue(
            isBlueTeam() ? VarbitID.BREW_SAN_BOTTLE_1 : VarbitID.BREW_DAN_BOTTLE_1
        );
        rumReady = rumState == RUM_READY_STATE;
        matchTime = cleanWidgetText(
            client.getWidget(InterfaceID.BrewOverlay.BREW_TIME_DISPLAY)
        );
        possibleRumsLeft = calculatePossibleRumsLeft(
            teamRumMade,
            parseMatchSeconds(matchTime),
            getBrewCycleSecondsRemaining()
        );
    }

    private void resetBrewStatus()
    {
        teamBitternuts = 0;
        teamSweetgrubs = 0;
        teamBuckets = 0;
        teamColouredWater = 0;
        teamBark = 0;
        previousTeamBitternuts = -1;
        previousTeamSweetgrubs = -1;
        previousTeamBuckets = -1;
        previousTeamColouredWater = -1;
        previousTeamBark = -1;
        boiler1Logs = 0;
        boiler2Logs = 0;
        boiler3Logs = 0;
        teamHopperRepairParts = 0;
        teamPipeRepairParts = 0;
        teamBridgeRepairParts = 0;
        teamRumMade = 0;
        rumLoadsAvailable = 0;
        possibleRumsLeft = 0;
        brewCycleEndTick = -1;
        teamHopperOnFire = false;
        teamPipesOnFire = false;
        teamBridgeOnFire = false;
        rumReady = false;
        matchTime = "-";
    }

    private static boolean decreased(int previous, int current)
    {
        return previous >= 0 && current < previous;
    }

    private static String cleanWidgetText(Widget widget)
    {
        if (widget == null || widget.getText() == null)
        {
            return "-";
        }

        String text = Text.removeTags(widget.getText()).trim();
        return text.isEmpty() ? "-" : text;
    }

    private boolean isBlueTeam()
    {
        return getLocalTeam() == BrewTeam.BLUE;
    }

    private BrewTeam getLocalTeam()
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment == null)
        {
            return BrewTeam.NONE;
        }

        Item headItem = equipment.getItem(EquipmentInventorySlot.HEAD.getSlotIdx());
        if (headItem == null)
        {
            return BrewTeam.NONE;
        }

        if (headItem.getId() == ItemID.BREW_BLUE_PIRATE_HAT)
        {
            return BrewTeam.BLUE;
        }

        return headItem.getId() == ItemID.BREW_RED_PIRATE_HAT
            ? BrewTeam.RED
            : BrewTeam.NONE;
    }

    private static int parseNonNegativeInteger(Widget widget)
    {
        if (widget == null)
        {
            return 0;
        }

        String text = widget.getText();
        if (text == null || text.isEmpty())
        {
            return 0;
        }

        try
        {
            return Math.max(Integer.parseInt(Text.removeTags(text).trim()), 0);
        }
        catch (NumberFormatException ex)
        {
            return 0;
        }
    }

    private void updateMonkeyAdviceState()
    {
        long now = System.currentTimeMillis();
        boolean relevantMenuVisible = config.showMonkeyDialogueHelper()
            && isTroubleBrewingVicinity()
            && findMonkeyChoice(MONKEY_CAREFUL_KEYWORD) != null
            && findMonkeyChoice(MONKEY_ANGRY_KEYWORD) != null;

        if (!relevantMenuVisible)
        {
            if (monkeyAdviceMenuVisible)
            {
                monkeyAdviceMenuVisible = false;
                if (activeMonkeyAdvice == MonkeyAdvice.CAREFUL)
                {
                    nextMonkeyAdvice = MonkeyAdvice.ANGRY;
                    carefulAdviceClosedAt = now;
                }
                else
                {
                    resetMonkeyAdviceCycle();
                }
            }
            else if (nextMonkeyAdvice == MonkeyAdvice.ANGRY
                && now - carefulAdviceClosedAt > MONKEY_PAIR_WINDOW_MS)
            {
                resetMonkeyAdviceCycle();
            }
            return;
        }

        if (monkeyAdviceMenuVisible)
        {
            return;
        }

        monkeyAdviceMenuVisible = true;
        if (nextMonkeyAdvice == MonkeyAdvice.ANGRY
            && now - carefulAdviceClosedAt <= MONKEY_PAIR_WINDOW_MS)
        {
            activeMonkeyAdvice = MonkeyAdvice.ANGRY;
        }
        else
        {
            activeMonkeyAdvice = MonkeyAdvice.CAREFUL;
            nextMonkeyAdvice = MonkeyAdvice.CAREFUL;
            carefulAdviceClosedAt = 0L;
        }
    }

    private void resetMonkeyAdviceCycle()
    {
        nextMonkeyAdvice = MonkeyAdvice.CAREFUL;
        activeMonkeyAdvice = MonkeyAdvice.CAREFUL;
        monkeyAdviceMenuVisible = false;
        carefulAdviceClosedAt = 0L;
    }

    private static boolean isTeamAttendant(int npcId)
    {
        return npcId == NpcID.SAN_FAN || npcId == NpcID.FANCY_DAN;
    }

    private static boolean isVicinityNpc(int npcId)
    {
        return isTeamAttendant(npcId) || npcId == NpcID.HONEST_JIMMY;
    }

    private static boolean isJoinCrewOption(String option)
    {
        if (option == null)
        {
            return false;
        }

        String normalized = Text.removeTags(option)
            .toLowerCase(Locale.ENGLISH)
            .replace('-', ' ')
            .trim();
        return "join crew".equals(normalized);
    }

    private static Widget findChoice(Widget choices, String keyword)
    {
        if (choices == null)
        {
            return null;
        }

        Widget choice = findChoice(choices.getChildren(), keyword);
        if (choice != null)
        {
            return choice;
        }

        return findChoice(choices.getNestedChildren(), keyword);
    }

    private static Widget findChoice(Widget[] choices, String keyword)
    {
        if (choices == null)
        {
            return null;
        }

        for (Widget choice : choices)
        {
            if (choice != null && !choice.isHidden() && choice.getText() != null)
            {
                String text = Text.removeTags(choice.getText()).toLowerCase(Locale.ENGLISH);
                if (text.contains(keyword))
                {
                    return choice;
                }
            }
        }

        return null;
    }

    Collection<HighlightedObject> getHighlightedObjects()
    {
        return Collections.unmodifiableCollection(highlightedObjects.values());
    }

    Collection<HighlightedNpc> getHighlightedNpcs()
    {
        return Collections.unmodifiableCollection(highlightedNpcs.values());
    }

    boolean isSwarmedSweetgrubMound(HighlightedObject highlightedObject)
    {
        return swarmedSweetgrubMounds.contains(highlightedObject.getTileObject());
    }

    boolean isTroubleBrewingSceneLoaded()
    {
        return !highlightedObjects.isEmpty();
    }

    int getLocalTeamObjectCount(ResourceType resourceType)
    {
        BrewTeam localTeam = getLocalTeam();
        return localTeam == BrewTeam.NONE
            ? 0
            : teamCounts(localTeam).getOrDefault(resourceType, 0);
    }

    boolean isLocalTeamObject(HighlightedObject highlightedObject)
    {
        BrewTeam localTeam = getLocalTeam();
        return localTeam != BrewTeam.NONE && highlightedObject.getTeam() == localTeam;
    }

    boolean isTroubleBrewingVicinity()
    {
        Player localPlayer = client.getLocalPlayer();
        if (client.getGameState() != GameState.LOGGED_IN || localPlayer == null)
        {
            return false;
        }

        return localPlayer.getWorldLocation().getRegionID() == TROUBLE_BREWING_REGION_ID
            || !vicinityNpcs.isEmpty()
            || isVisible(InterfaceID.BrewOverlay.UNIVERSE)
            || isVisible(InterfaceID.BrewWaitingRoomOverlay.UNIVERSE);
    }

    boolean isTroubleBrewingMatchActive()
    {
        return isVisible(InterfaceID.BrewOverlay.UNIVERSE);
    }

    int getPiecesOfEight()
    {
        return piecesOfEight;
    }

    int getExpectedPiecesOfEight()
    {
        return expectedPiecesOfEight;
    }

    int getTeamBitternuts()
    {
        return teamBitternuts;
    }

    int getTeamSweetgrubs()
    {
        return teamSweetgrubs;
    }

    int getTeamBuckets()
    {
        return teamBuckets;
    }

    int getTeamColouredWater()
    {
        return teamColouredWater;
    }

    int getTeamBark()
    {
        return teamBark;
    }

    int getPossibleRumsLeft()
    {
        return possibleRumsLeft;
    }

    static int remainingRums(int teamRumMade)
    {
        return Math.max(MAX_RUM_PER_GAME - Math.max(teamRumMade, 0), 0);
    }

    static int calculatePossibleRumsLeft(
        int teamRumMade,
        int matchSecondsRemaining,
        int cycleSecondsRemaining)
    {
        int capacityRemaining = remainingRums(teamRumMade);
        if (capacityRemaining == 0 || matchSecondsRemaining < 0)
        {
            return capacityRemaining;
        }

        int productionSeconds = matchSecondsRemaining - RUM_COLLECTION_CUSHION_SECONDS;
        if (productionSeconds <= 0)
        {
            return 0;
        }

        int possibleCycles;
        if (cycleSecondsRemaining >= 0)
        {
            if (cycleSecondsRemaining > productionSeconds)
            {
                return 0;
            }

            possibleCycles = 1 + fullCyclesInSeconds(
                productionSeconds - cycleSecondsRemaining
            );
        }
        else
        {
            possibleCycles = fullCyclesInSeconds(productionSeconds);
        }

        return Math.min(capacityRemaining, possibleCycles);
    }

    private static int fullCyclesInSeconds(int seconds)
    {
        // One game tick is 0.6 seconds, represented as 3 / 5 to avoid rounding.
        return (Math.max(seconds, 0) * 5) / (BREW_CYCLE_TICKS * 3);
    }

    static int parseMatchSeconds(String text)
    {
        if (text == null)
        {
            return -1;
        }

        Matcher matcher = MATCH_TIME_PATTERN.matcher(text);
        if (!matcher.find())
        {
            return -1;
        }

        try
        {
            int minutes = Integer.parseInt(matcher.group(1));
            String secondsText = matcher.group(2);
            int seconds = secondsText == null ? 0 : Integer.parseInt(secondsText);
            return Math.max((minutes * 60) + Math.min(seconds, 59), 0);
        }
        catch (NumberFormatException ex)
        {
            return -1;
        }
    }

    int getRumLoadsAvailable()
    {
        return rumLoadsAvailable;
    }

    boolean hasLocalTeamFire()
    {
        return teamHopperOnFire || teamPipesOnFire || teamBridgeOnFire;
    }

    int getLocalTeamRepairParts(ResourceType resourceType)
    {
        switch (resourceType)
        {
            case LUMBER_REPAIR:
                return teamHopperRepairParts;
            case PIPE_REPAIR:
                return teamPipeRepairParts;
            case DAMAGE_REPAIR:
                return teamBridgeRepairParts;
            default:
                return 0;
        }
    }

    boolean isRumReady()
    {
        return rumReady;
    }

    String getMatchTime()
    {
        return matchTime;
    }

    int getBrewCycleSecondsRemaining()
    {
        if (brewCycleEndTick < 0 || rumReady)
        {
            return -1;
        }

        int ticksRemaining = Math.max(brewCycleEndTick - client.getTickCount(), 0);
        return ticksRemaining == 0 ? 0 : Math.max((ticksRemaining * 3) / 5, 1);
    }

    boolean needsBoilerLogs()
    {
        return isTroubleBrewingMatchActive()
            && (getLocalTeamObjectCount(ResourceType.BOILER_EMPTY) > 0
                || boiler1Logs < MAX_BOILER_LOGS
                || boiler2Logs < MAX_BOILER_LOGS
                || boiler3Logs < MAX_BOILER_LOGS);
    }

    boolean needsBoilerLighting()
    {
        return isTroubleBrewingMatchActive()
            && getLocalTeamObjectCount(ResourceType.BOILER_UNLIT) > 0;
    }

    boolean needsBoilerAction()
    {
        return getLocalTeamObjectCount(ResourceType.BOILER_EMPTY) > 0
            || getLocalTeamObjectCount(ResourceType.BOILER_UNLIT) > 0;
    }

    int getLowestBoilerLogCount()
    {
        return Math.min(boiler1Logs, Math.min(boiler2Logs, boiler3Logs));
    }

    int getBoilerLogCount(HighlightedObject highlightedObject)
    {
        BrewTeam localTeam = getLocalTeam();
        if (localTeam == BrewTeam.NONE || highlightedObject.getTeam() != localTeam)
        {
            return -1;
        }

        int counterIndex = getBoilerCounterIndex(
            highlightedObject.getTileObject().getId(),
            localTeam == BrewTeam.BLUE
        );
        switch (counterIndex)
        {
            case 0:
                return boiler1Logs;
            case 1:
                return boiler2Logs;
            case 2:
                return boiler3Logs;
            default:
                return -1;
        }
    }

    static int getBoilerCounterIndex(int objectId, boolean blueTeam)
    {
        switch (objectId)
        {
            case ObjectID.BREW_STILL_BOILER:
            case ObjectID.BREW_STILL_BOILER_LOGS:
            case ObjectID.BREW_STILL_BOILER_FIRE:
                return blueTeam ? 0 : 2;
            case ObjectID.BREW_STILL_BOILER_CORNER:
            case ObjectID.BREW_STILL_BOILER_CORNER_LOGS:
            case ObjectID.BREW_STILL_BOILER_CORNER_FIRE:
                return 1;
            case ObjectID.BREW_STILL_BOILER_CORNER_MIRROR:
            case ObjectID.BREW_STILL_BOILER_CORNER_LOGS_MIRROR:
            case ObjectID.BREW_STILL_BOILER_CORNER_FIRE_MIRROR:
                return blueTeam ? 2 : 0;
            default:
                return -1;
        }
    }

    String getBrewStatusAction()
    {
        if (rumReady)
        {
            return "Collect rum";
        }

        boolean needsFuel = getLocalTeamObjectCount(ResourceType.BOILER_EMPTY) > 0;
        boolean needsLighting = needsBoilerLighting();
        if (needsFuel && needsLighting)
        {
            return "Fuel & light boilers";
        }
        if (needsFuel)
        {
            return "Add logs";
        }
        if (needsLighting)
        {
            return "Light boilers";
        }

        int possibleRumsLeft = getPossibleRumsLeft();
        String ingredientAction = lowestIngredientAction(
            possibleRumsLeft,
            teamBitternuts,
            teamSweetgrubs,
            teamBuckets,
            teamColouredWater,
            teamBark
        );
        if (ingredientAction != null)
        {
            return ingredientAction;
        }
        if (possibleRumsLeft == 0)
        {
            return "Run supplied";
        }
        if (needsBoilerLogs())
        {
            return "Top up boiler logs";
        }
        if (getBrewCycleSecondsRemaining() >= 0)
        {
            return "Brewing";
        }
        return "Keep supplies flowing";
    }

    static String lowestIngredientAction(
        int possibleRumsLeft,
        int bitternuts,
        int sweetgrubs,
        int buckets,
        int colouredWater,
        int bark)
    {
        if (possibleRumsLeft <= 0)
        {
            return null;
        }

        String lowestAction = null;
        int lowestCurrent = 0;
        int lowestRequired = 1;

        if (bitternuts < possibleRumsLeft)
        {
            lowestAction = "Fill bitternuts";
            lowestCurrent = bitternuts;
            lowestRequired = possibleRumsLeft;
        }
        if (sweetgrubs < possibleRumsLeft
            && isLowerSupplyRatio(
                sweetgrubs,
                possibleRumsLeft,
                lowestAction,
                lowestCurrent,
                lowestRequired))
        {
            lowestAction = "Fill sweetgrubs";
            lowestCurrent = sweetgrubs;
            lowestRequired = possibleRumsLeft;
        }

        int bucketTarget = possibleRumsLeft * 5;
        if (buckets < bucketTarget
            && isLowerSupplyRatio(
                buckets,
                bucketTarget,
                lowestAction,
                lowestCurrent,
                lowestRequired))
        {
            lowestAction = "Fill water buckets";
            lowestCurrent = buckets;
            lowestRequired = bucketTarget;
        }

        int colouredWaterTarget = possibleRumsLeft * 3;
        if (colouredWater < colouredWaterTarget
            && isLowerSupplyRatio(
                colouredWater,
                colouredWaterTarget,
                lowestAction,
                lowestCurrent,
                lowestRequired))
        {
            lowestAction = "Fill coloured water";
            lowestCurrent = colouredWater;
            lowestRequired = colouredWaterTarget;
        }
        if (bark < possibleRumsLeft
            && isLowerSupplyRatio(
                bark,
                possibleRumsLeft,
                lowestAction,
                lowestCurrent,
                lowestRequired))
        {
            lowestAction = "Fill scrapey bark";
        }

        return lowestAction;
    }

    private static boolean isLowerSupplyRatio(
        int current,
        int required,
        String lowestAction,
        int lowestCurrent,
        int lowestRequired)
    {
        return lowestAction == null
            || (long) current * lowestRequired < (long) lowestCurrent * required;
    }

    Widget getRecommendedMonkeyChoice()
    {
        if (!monkeyAdviceMenuVisible)
        {
            return null;
        }

        return findMonkeyChoice(activeMonkeyAdvice == MonkeyAdvice.CAREFUL
            ? MONKEY_CAREFUL_KEYWORD
            : MONKEY_ANGRY_KEYWORD);
    }

    private Widget findMonkeyChoice(String keyword)
    {
        Widget choices = client.getWidget(InterfaceID.Chatmenu.OPTIONS);
        return choices == null || choices.isHidden() ? null : findChoice(choices, keyword);
    }

    private boolean isVisible(int componentId)
    {
        Widget widget = client.getWidget(componentId);
        return widget != null && !widget.isHidden();
    }

    @Provides
    TroubleBrewingHighlighterConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(TroubleBrewingHighlighterConfig.class);
    }

    static final class HighlightedObject
    {
        private final TileObject tileObject;
        private final ResourceType resourceType;
        private final BrewTeam team;

        private HighlightedObject(
            TileObject tileObject,
            ResourceType resourceType,
            BrewTeam team)
        {
            this.tileObject = tileObject;
            this.resourceType = resourceType;
            this.team = team;
        }

        TileObject getTileObject()
        {
            return tileObject;
        }

        ResourceType getResourceType()
        {
            return resourceType;
        }

        private BrewTeam getTeam()
        {
            return team;
        }

    }

    static final class HighlightedNpc
    {
        private final NPC npc;
        private final ResourceType resourceType;

        private HighlightedNpc(NPC npc, ResourceType resourceType)
        {
            this.npc = npc;
            this.resourceType = resourceType;
        }

        NPC getNpc()
        {
            return npc;
        }

        ResourceType getResourceType()
        {
            return resourceType;
        }
    }

    private enum MonkeyAdvice
    {
        CAREFUL,
        ANGRY
    }

    private enum BrewTeam
    {
        NONE,
        RED,
        BLUE
    }
}
