package com.tbhelper;

import com.google.inject.Provides;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.GroundObject;
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
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
        name = "Trouble Brewing Highlighter",
        description = "Colour-coded highlights for Trouble Brewing resources and processing stations",
        tags = {"trouble", "brewing", "minigame", "highlight", "helper"}
)
public class TroubleBrewingHighlighterPlugin extends Plugin
{
    private static final int BOOTSTRAP_SCAN_TICKS = 20;

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

    private final Map<TileObject, HighlightedObject> highlightedObjects = new IdentityHashMap<>();
    private final Map<NPC, HighlightedNpc> highlightedNpcs = new IdentityHashMap<>();
    private int bootstrapTicksRemaining;

    @Override
    protected void startUp()
    {
        overlayManager.add(overlay);
        overlayManager.add(inventoryOverlay);
        beginBootstrapScan();
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        overlayManager.remove(inventoryOverlay);
        bootstrapTicksRemaining = 0;
        clearCache();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        GameState gameState = event.getGameState();

        if (gameState == GameState.LOGGED_IN)
        {
            beginBootstrapScan();
        }
        else
        {
            bootstrapTicksRemaining = 0;
            clearCache();
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
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
}
