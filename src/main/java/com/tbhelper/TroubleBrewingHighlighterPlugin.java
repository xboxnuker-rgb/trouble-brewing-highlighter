package com.tbhelper;

import com.google.inject.Provides;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
    private MonkeyDialogueOverlay monkeyDialogueOverlay;

    @Inject
    private TroubleBrewingHighlighterConfig config;

    private final Map<TileObject, HighlightedObject> highlightedObjects = new IdentityHashMap<>();
    private final Map<NPC, HighlightedNpc> highlightedNpcs = new IdentityHashMap<>();
    private final Set<NPC> vicinityNpcs = Collections.newSetFromMap(new IdentityHashMap<>());
    private int bootstrapTicksRemaining;
    private int piecesOfEight;
    private int expectedPiecesOfEight;
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
        overlayManager.remove(monkeyDialogueOverlay);
        bootstrapTicksRemaining = 0;
        piecesOfEight = 0;
        expectedPiecesOfEight = 0;
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
            resetMonkeyAdviceCycle();
            clearCache();
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        updateExpectedPiecesOfEight();
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
        highlightedObjects.remove(event.getGameObject());
    }

    @Subscribe
    public void onWallObjectSpawned(WallObjectSpawned event)
    {
        trackObject(event.getWallObject());
    }

    @Subscribe
    public void onWallObjectDespawned(WallObjectDespawned event)
    {
        highlightedObjects.remove(event.getWallObject());
    }

    @Subscribe
    public void onDecorativeObjectSpawned(DecorativeObjectSpawned event)
    {
        trackObject(event.getDecorativeObject());
    }

    @Subscribe
    public void onDecorativeObjectDespawned(DecorativeObjectDespawned event)
    {
        highlightedObjects.remove(event.getDecorativeObject());
    }

    @Subscribe
    public void onGroundObjectSpawned(GroundObjectSpawned event)
    {
        trackObject(event.getGroundObject());
    }

    @Subscribe
    public void onGroundObjectDespawned(GroundObjectDespawned event)
    {
        highlightedObjects.remove(event.getGroundObject());
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
        if (resourceType != null)
        {
            highlightedObjects.put(
                    object,
                    new HighlightedObject(object, resourceType)
            );
        }
    }

    private void trackNpc(NPC npc)
    {
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

    private void clearCache()
    {
        highlightedObjects.clear();
        highlightedNpcs.clear();
        vicinityNpcs.clear();
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
        int rumScore = parseNonNegativeInteger(teamScoreWidget);
        expectedPiecesOfEight = piecesOfEight + contribution + (rumScore * 10);
    }

    private boolean isBlueTeam()
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment == null)
        {
            return false;
        }

        Item headItem = equipment.getItem(EquipmentInventorySlot.HEAD.getSlotIdx());
        return headItem != null && headItem.getId() == ItemID.BREW_BLUE_PIRATE_HAT;
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

    boolean isTroubleBrewingSceneLoaded()
    {
        return !highlightedObjects.isEmpty();
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
        private HighlightedObject(TileObject tileObject, ResourceType resourceType)
        {
            this.tileObject = tileObject;
            this.resourceType = resourceType;
        }

        TileObject getTileObject()
        {
            return tileObject;
        }

        ResourceType getResourceType()
        {
            return resourceType;
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
}
