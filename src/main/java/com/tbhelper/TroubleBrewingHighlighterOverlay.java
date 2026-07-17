package com.tbhelper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.WallObject;
import net.runelite.api.WorldView;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class TroubleBrewingHighlighterOverlay extends Overlay
{
    private final Client client;
    private final TroubleBrewingHighlighterPlugin plugin;
    private final TroubleBrewingHighlighterConfig config;

    @Inject
    private TroubleBrewingHighlighterOverlay(
        Client client,
        TroubleBrewingHighlighterPlugin plugin,
        TroubleBrewingHighlighterConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        Stroke stroke = new BasicStroke((float) config.outlineWidth());

        for (TroubleBrewingHighlighterPlugin.HighlightedObject highlightedObject
            : plugin.getHighlightedObjects())
        {
            ResourceType resourceType = highlightedObject.getResourceType();
            TileObject tileObject = highlightedObject.getTileObject();
            if (!isEnabled(resourceType) || !isOnActivePlane(tileObject))
            {
                continue;
            }

            renderObject(
                graphics,
                tileObject,
                colourFor(resourceType),
                stroke
            );
        }

        for (TroubleBrewingHighlighterPlugin.HighlightedNpc highlightedNpc
            : plugin.getHighlightedNpcs())
        {
            ResourceType resourceType = highlightedNpc.getResourceType();
            NPC npc = highlightedNpc.getNpc();
            if (!isEnabled(resourceType) || !isOnActivePlane(npc))
            {
                continue;
            }

            renderNpc(graphics, npc, colourFor(resourceType), stroke);
        }

        // Bucket, bowl, axe, bark, rat meat and sweetgrubs are generic item IDs.
        // Only render them while verified Trouble Brewing scene objects are loaded.
        if (plugin.isTroubleBrewingSceneLoaded() && config.drawTile())
        {
            for (TroubleBrewingHighlighterPlugin.HighlightedGroundItem highlightedItem
                : plugin.getHighlightedGroundItems())
            {
                ResourceType resourceType = highlightedItem.getResourceType();
                Tile tile = highlightedItem.getTile();
                if (!isEnabled(resourceType) || !isOnActivePlane(tile))
                {
                    continue;
                }

                renderGroundItemTile(
                    graphics,
                    tile,
                    colourFor(resourceType),
                    stroke
                );
            }
        }

        return null;
    }


    private WorldView getActiveWorldView()
    {
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer != null && localPlayer.getWorldView() != null)
        {
            return localPlayer.getWorldView();
        }

        return client.getTopLevelWorldView();
    }

    private boolean isOnActivePlane(TileObject object)
    {
        WorldView worldView = getActiveWorldView();
        return worldView != null
            && object.getWorldView() != null
            && object.getWorldView().getId() == worldView.getId()
            && object.getPlane() == worldView.getPlane();
    }

    private boolean isOnActivePlane(Tile tile)
    {
        WorldView worldView = getActiveWorldView();
        return worldView != null
            && tile.getLocalLocation().getWorldView() == worldView.getId()
            && tile.getPlane() == worldView.getPlane();
    }

    private boolean isOnActivePlane(NPC npc)
    {
        WorldView worldView = getActiveWorldView();
        return worldView != null
            && npc.getWorldView() != null
            && npc.getWorldView().getId() == worldView.getId()
            && npc.getWorldLocation().getPlane() == worldView.getPlane();
    }

    private void renderNpc(Graphics2D graphics, NPC npc, Color colour, Stroke stroke)
    {
        if (config.drawTile())
        {
            renderShape(graphics, npc.getCanvasTilePoly(), colour, stroke);
        }

        if (config.drawHull())
        {
            renderShape(graphics, npc.getConvexHull(), colour, stroke);
        }
    }

    private void renderObject(
            Graphics2D graphics,
            TileObject object,
            Color colour,
            Stroke stroke)
    {
        // Draw the tile whenever Draw Tile is enabled.
        // This is independent of whether the object's hull is available.
        if (config.drawTile())
        {
            renderShape(
                    graphics,
                    object.getCanvasTilePoly(),
                    colour,
                    stroke
            );
        }

        if (!config.drawHull())
        {
            return;
        }

        if (object instanceof GameObject)
        {
            renderShape(
                    graphics,
                    ((GameObject) object).getConvexHull(),
                    colour,
                    stroke
            );
        }
        else if (object instanceof GroundObject)
        {
            renderShape(
                    graphics,
                    ((GroundObject) object).getConvexHull(),
                    colour,
                    stroke
            );
        }
        else if (object instanceof WallObject)
        {
            WallObject wallObject = (WallObject) object;

            renderShape(
                    graphics,
                    wallObject.getConvexHull(),
                    colour,
                    stroke
            );

            renderShape(
                    graphics,
                    wallObject.getConvexHull2(),
                    colour,
                    stroke
            );
        }
        else if (object instanceof DecorativeObject)
        {
            DecorativeObject decorativeObject = (DecorativeObject) object;

            renderShape(
                    graphics,
                    decorativeObject.getConvexHull(),
                    colour,
                    stroke
            );

            renderShape(
                    graphics,
                    decorativeObject.getConvexHull2(),
                    colour,
                    stroke
            );
        }
    }

    private void renderGroundItemTile(Graphics2D graphics, Tile tile, Color colour, Stroke stroke)
    {
        Polygon tilePolygon = Perspective.getCanvasTilePoly(client, tile.getLocalLocation());
        renderShape(graphics, tilePolygon, colour, stroke);
    }

    private boolean renderShape(Graphics2D graphics, Shape shape, Color colour, Stroke stroke)
    {
        if (shape == null)
        {
            return false;
        }

        graphics.setStroke(stroke);
        graphics.setColor(withAlpha(colour, config.fillOpacity()));
        graphics.fill(shape);
        graphics.setColor(colour);
        graphics.draw(shape);
        return true;
    }

    private boolean isEnabled(ResourceType resourceType)
    {
        switch (resourceType)
        {
            case WATER:
                return config.showWater();
            case COLOURED_WATER:
                return config.showColouredWater();
            case HOPPERS:
                return config.showHoppers();
            case BARK:
                return config.showBark();
            case BAIT:
                return config.showBait();
            case GRUBS:
                return config.showGrubs();
            case FLOWERS:
                return config.showFlowers();
            case BITTERNUTS:
                return config.showBitternuts();
            case RUM:
                return config.showRum();
            default:
                return false;
        }
    }

    private Color colourFor(ResourceType resourceType)
    {
        switch (resourceType)
        {
            case WATER:
                return config.waterColor();
            case COLOURED_WATER:
                return config.colouredWaterColor();
            case HOPPERS:
                return config.hopperColor();
            case BARK:
                return config.barkColor();
            case BAIT:
                return config.baitColor();
            case GRUBS:
                return config.grubColor();
            case FLOWERS:
                return config.flowerColor();
            case BITTERNUTS:
                return config.bitternutColor();
            case RUM:
                return config.rumColor();
            default:
                return Color.WHITE;
        }
    }

    private static Color withAlpha(Color colour, int alpha)
    {
        return new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), alpha);
    }
}
