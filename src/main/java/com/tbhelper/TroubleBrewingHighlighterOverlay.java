package com.tbhelper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.TileObject;
import net.runelite.api.WallObject;
import net.runelite.api.WorldView;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class TroubleBrewingHighlighterOverlay extends Overlay
{
    private static final long FLASH_INTERVAL_MS = 500L;
    private static final int BOILER_TEXT_Z_OFFSET = 100;
    private static final int HOPPER_TEXT_Z_OFFSET = 110;
    private static final int DAMAGE_TEXT_Z_OFFSET = 100;
    private static final int GUIDANCE_ICON_SIZE = 32;
    private static final int MAX_BOILER_LOGS = 10;
    private static final Color BOILER_LOW_COLOR = new Color(255, 145, 35);
    private static final Color BOILER_FULL_COLOR = new Color(85, 220, 100);
    private static final Color SUPPLY_EMPTY_COLOR = new Color(235, 80, 80);
    private final Client client;
    private final ItemManager itemManager;
    private final TroubleBrewingHighlighterPlugin plugin;
    private final TroubleBrewingHighlighterConfig config;
    private final Map<Integer, BufferedImage> itemImages = new HashMap<>();

    @Inject
    private TroubleBrewingHighlighterOverlay(
        Client client,
        ItemManager itemManager,
        TroubleBrewingHighlighterPlugin plugin,
        TroubleBrewingHighlighterConfig config)
    {
        this.client = client;
        this.itemManager = itemManager;
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        Stroke stroke = new BasicStroke((float) config.outlineWidth());
        boolean flashOn = isFlashOn();
        boolean showStationAmounts = config.showStationAmounts()
            && plugin.isTroubleBrewingMatchActive();
        Map<ResourceType, TroubleBrewingHighlighterPlugin.HighlightedObject> repairGuidance =
            new EnumMap<>(ResourceType.class);

        for (TroubleBrewingHighlighterPlugin.HighlightedObject highlightedObject
            : plugin.getHighlightedObjects())
        {
            ResourceType resourceType = highlightedObject.getResourceType();
            TileObject tileObject = highlightedObject.getTileObject();
            if (!isOnActivePlane(tileObject))
            {
                continue;
            }

            if (isEnabled(highlightedObject, flashOn))
            {
                renderObject(
                    graphics,
                    tileObject,
                    colourFor(resourceType),
                    stroke
                );
            }

            renderBoilerLogs(
                graphics,
                highlightedObject,
                showStationAmounts && config.showBoilerFuel()
            );
            renderHopperAmount(graphics, highlightedObject, showStationAmounts);
            renderFireGuidance(graphics, highlightedObject);
            collectRepairGuidance(highlightedObject, repairGuidance);
        }

        for (TroubleBrewingHighlighterPlugin.HighlightedObject highlightedObject
            : repairGuidance.values())
        {
            renderRepairGuidance(graphics, highlightedObject);
        }

        for (TroubleBrewingHighlighterPlugin.HighlightedNpc highlightedNpc
            : plugin.getHighlightedNpcs())
        {
            ResourceType resourceType = highlightedNpc.getResourceType();
            NPC npc = highlightedNpc.getNpc();
            if (!isEnabled(resourceType, flashOn) || !isOnActivePlane(npc))
            {
                continue;
            }

            renderNpc(graphics, npc, colourFor(resourceType), stroke);
        }

        return null;
    }

    private void renderBoilerLogs(
        Graphics2D graphics,
        TroubleBrewingHighlighterPlugin.HighlightedObject highlightedObject,
        boolean showBoilerLogs)
    {
        ResourceType resourceType = highlightedObject.getResourceType();
        if (!showBoilerLogs || !isBoiler(resourceType))
        {
            return;
        }

        int logCount = plugin.getBoilerLogCount(highlightedObject);
        if (logCount < 0)
        {
            return;
        }

        int clampedLogCount = Math.min(logCount, MAX_BOILER_LOGS);
        int logsNeeded = MAX_BOILER_LOGS - clampedLogCount;
        String logLabel = clampedLogCount == 1 ? "1 log" : clampedLogCount + " logs";
        String text = logsNeeded == 0
            ? logLabel + " | full"
            : logLabel + " | add " + logsNeeded;
        TileObject tileObject = highlightedObject.getTileObject();
        Font previousFont = graphics.getFont();
        graphics.setFont(previousFont.deriveFont(Font.BOLD));
        Point textLocation = Perspective.getCanvasTextLocation(
            client,
            graphics,
            tileObject.getLocalLocation(),
            text,
            BOILER_TEXT_Z_OFFSET
        );
        if (textLocation == null)
        {
            graphics.setFont(previousFont);
            return;
        }

        OverlayUtil.renderTextLocation(
            graphics,
            textLocation,
            text,
            logsNeeded == 0 ? BOILER_FULL_COLOR : BOILER_LOW_COLOR
        );
        if (resourceType == ResourceType.BOILER_UNLIT)
        {
            renderItemIcon(graphics, textLocation, text, ItemID.TINDERBOX);
        }
        graphics.setFont(previousFont);
    }

    private void renderItemIcon(
        Graphics2D graphics,
        Point textLocation,
        String text,
        int itemId)
    {
        BufferedImage image = itemImages.get(itemId);
        if (image == null)
        {
            image = itemManager.getImage(itemId);
            if (image != null)
            {
                itemImages.put(itemId, image);
            }
        }
        if (image == null)
        {
            return;
        }

        int textWidth = graphics.getFontMetrics().stringWidth(text);
        int iconX = textLocation.getX() + ((textWidth - GUIDANCE_ICON_SIZE) / 2);
        int iconY = textLocation.getY() + 4;
        graphics.drawImage(
            image,
            iconX,
            iconY,
            GUIDANCE_ICON_SIZE,
            GUIDANCE_ICON_SIZE,
            null
        );
    }

    private void renderHopperAmount(
        Graphics2D graphics,
        TroubleBrewingHighlighterPlugin.HighlightedObject highlightedObject,
        boolean showStationAmounts)
    {
        if (!showStationAmounts || !plugin.isLocalTeamObject(highlightedObject))
        {
            return;
        }

        int current;
        int perRum;
        int itemId;
        switch (highlightedObject.getTileObject().getId())
        {
            case ObjectID.BREW_WATER_SIGN:
                current = plugin.getTeamBuckets();
                perRum = 5;
                itemId = ItemID.BUCKET_WATER;
                break;
            case ObjectID.BREW_COLOUR_WATER_BLUE_SIGN:
                current = plugin.getTeamColouredWater();
                perRum = 3;
                itemId = ItemID.BREW_BOWL_BLUE;
                break;
            case ObjectID.BREW_COLOUR_WATER_RED_SIGN:
                current = plugin.getTeamColouredWater();
                perRum = 3;
                itemId = ItemID.BREW_BOWL_RED;
                break;
            case ObjectID.BREW_BARK_SIGN:
                current = plugin.getTeamBark();
                perRum = 1;
                itemId = ItemID.BREW_SCRAPEY_BARK;
                break;
            case ObjectID.BREW_SWEET_GRUBS_SIGN:
                current = plugin.getTeamSweetgrubs();
                perRum = 1;
                itemId = ItemID.BREW_SWEETGRUBS;
                break;
            case ObjectID.BREW_BITTERNUT_SIGN:
                current = plugin.getTeamBitternuts();
                perRum = 1;
                itemId = ItemID.BREW_BITTERNUT;
                break;
            default:
                return;
        }

        int required = plugin.getRemainingRums() * perRum;
        String text = current + " / " + required;
        TileObject tileObject = highlightedObject.getTileObject();
        Font previousFont = graphics.getFont();
        graphics.setFont(previousFont.deriveFont(Font.BOLD));
        Point textLocation = Perspective.getCanvasTextLocation(
            client,
            graphics,
            tileObject.getLocalLocation(),
            text,
            HOPPER_TEXT_Z_OFFSET
        );
        if (textLocation != null)
        {
            OverlayUtil.renderTextLocation(
                graphics,
                textLocation,
                text,
                supplyColour(current, required)
            );
            renderItemIcon(graphics, textLocation, text, itemId);
        }
        graphics.setFont(previousFont);
    }

    private void renderFireGuidance(
        Graphics2D graphics,
        TroubleBrewingHighlighterPlugin.HighlightedObject highlightedObject)
    {
        if (!config.showDamageRepair()
            || !plugin.isTroubleBrewingMatchActive()
            || !plugin.isLocalTeamObject(highlightedObject)
            || highlightedObject.getResourceType() != ResourceType.ACTIVE_FIRE)
        {
            return;
        }

        renderGuidance(
            graphics,
            highlightedObject,
            "Douse fire",
            ItemID.BUCKET_WATER
        );
    }

    private void collectRepairGuidance(
        TroubleBrewingHighlighterPlugin.HighlightedObject highlightedObject,
        Map<ResourceType, TroubleBrewingHighlighterPlugin.HighlightedObject> repairGuidance)
    {
        if (!config.showDamageRepair()
            || !plugin.isTroubleBrewingMatchActive()
            || !plugin.isLocalTeamObject(highlightedObject))
        {
            return;
        }

        ResourceType resourceType = highlightedObject.getResourceType();
        int partsRemaining = repairPartsRemaining(highlightedObject.getTileObject().getId());
        if (partsRemaining <= 0)
        {
            return;
        }

        TroubleBrewingHighlighterPlugin.HighlightedObject current = repairGuidance.get(resourceType);
        if (current == null
            || repairPartsRemaining(current.getTileObject().getId()) < partsRemaining)
        {
            repairGuidance.put(resourceType, highlightedObject);
        }
    }

    private void renderRepairGuidance(
        Graphics2D graphics,
        TroubleBrewingHighlighterPlugin.HighlightedObject highlightedObject)
    {
        ResourceType resourceType = highlightedObject.getResourceType();
        int partsRemaining = repairPartsRemaining(highlightedObject.getTileObject().getId());
        int itemId;
        String text;
        switch (resourceType)
        {
            case PIPE_REPAIR:
                itemId = ItemID.BREW_PIPE_SECTION;
                text = partsRemaining + (partsRemaining == 1
                    ? " pipe to repair"
                    : " pipes to repair");
                break;
            case LUMBER_REPAIR:
                itemId = ItemID.BREW_LUMBER_PATCH;
                text = partsRemaining + (partsRemaining == 1
                    ? " patch to repair"
                    : " patches to repair");
                break;
            case DAMAGE_REPAIR:
                itemId = ItemID.BREW_BRIDGE_SECTION;
                text = partsRemaining + (partsRemaining == 1
                    ? " bridge part to repair"
                    : " bridge parts to repair");
                break;
            default:
                return;
        }

        renderGuidance(graphics, highlightedObject, text, itemId);
    }

    private void renderGuidance(
        Graphics2D graphics,
        TroubleBrewingHighlighterPlugin.HighlightedObject highlightedObject,
        String text,
        int itemId)
    {
        ResourceType resourceType = highlightedObject.getResourceType();

        TileObject tileObject = highlightedObject.getTileObject();
        Font previousFont = graphics.getFont();
        graphics.setFont(previousFont.deriveFont(Font.BOLD));
        Point textLocation = Perspective.getCanvasTextLocation(
            client,
            graphics,
            tileObject.getLocalLocation(),
            text,
            DAMAGE_TEXT_Z_OFFSET
        );
        if (textLocation != null)
        {
            OverlayUtil.renderTextLocation(graphics, textLocation, text, colourFor(resourceType));
            renderItemIcon(graphics, textLocation, text, itemId);
        }
        graphics.setFont(previousFont);
    }

    static int repairPartsRemaining(int objectId)
    {
        switch (objectId)
        {
            case ObjectID.BREW_PIPES_RED_DESTROYED:
            case ObjectID.BREW_HOPPER_RED_DESTROYED:
            case ObjectID.BREW_BRIDGE_RED_DESTROYED:
            case ObjectID.BREW_PIPES_BLUE_DESTROYED:
            case ObjectID.BREW_HOPPER_BLUE_DESTROYED:
            case ObjectID.BREW_BRIDGE_BLUE_DESTROYED:
                return 3;
            case ObjectID.BREW_PIPES_RED_DAMAGED_1:
            case ObjectID.BREW_PIPES_RED_WET_1:
            case ObjectID.BREW_HOPPER_RED_DAMAGED_1:
            case ObjectID.BREW_HOPPER_RED_WET_1:
            case ObjectID.BREW_BRIDGE_RED_DAMAGED_1:
            case ObjectID.BREW_BRIDGE_RED_WET_1:
            case ObjectID.BREW_PIPES_BLUE_DAMAGED_1:
            case ObjectID.BREW_PIPES_BLUE_WET_1:
            case ObjectID.BREW_HOPPER_BLUE_DAMAGED_1:
            case ObjectID.BREW_HOPPER_BLUE_WET_1:
            case ObjectID.BREW_BRIDGE_BLUE_DAMAGED_1:
            case ObjectID.BREW_BRIDGE_BLUE_WET_1:
                return 2;
            case ObjectID.BREW_PIPES_RED_DAMAGED_2:
            case ObjectID.BREW_PIPES_RED_WET_2:
            case ObjectID.BREW_HOPPER_RED_DAMAGED_2:
            case ObjectID.BREW_HOPPER_RED_WET_2:
            case ObjectID.BREW_BRIDGE_RED_DAMAGED_2:
            case ObjectID.BREW_BRIDGE_RED_WET_2:
            case ObjectID.BREW_PIPES_BLUE_DAMAGED_2:
            case ObjectID.BREW_PIPES_BLUE_WET_2:
            case ObjectID.BREW_HOPPER_BLUE_DAMAGED_2:
            case ObjectID.BREW_HOPPER_BLUE_WET_2:
            case ObjectID.BREW_BRIDGE_BLUE_DAMAGED_2:
            case ObjectID.BREW_BRIDGE_BLUE_WET_2:
                return 1;
            case ObjectID.BREW_WATER_PUMP_DAMAGED:
                return 1;
            default:
                return 0;
        }
    }

    private static Color supplyColour(int current, int required)
    {
        if (required <= 0 || current >= required)
        {
            return BOILER_FULL_COLOR;
        }
        return current <= 0 ? SUPPLY_EMPTY_COLOR : BOILER_LOW_COLOR;
    }

    private static boolean isBoiler(ResourceType resourceType)
    {
        return resourceType == ResourceType.BOILER_EMPTY
            || resourceType == ResourceType.BOILER_UNLIT
            || resourceType == ResourceType.BOILER_ACTIVE;
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

    private boolean isFlashOn()
    {
        return (System.currentTimeMillis() / FLASH_INTERVAL_MS) % 2L == 0L;
    }

    private boolean isEnabled(ResourceType resourceType, boolean flashOn)
    {
        return isCategoryEnabled(resourceType)
            && (!isFlashing(resourceType) || flashOn);
    }

    private boolean isEnabled(
        TroubleBrewingHighlighterPlugin.HighlightedObject highlightedObject,
        boolean flashOn)
    {
        ResourceType resourceType = highlightedObject.getResourceType();
        boolean flashing = isFlashing(resourceType)
            && (!isTeamSpecificDamage(resourceType)
                || plugin.isLocalTeamObject(highlightedObject));
        return isCategoryEnabled(resourceType) && (!flashing || flashOn);
    }

    private static boolean isTeamSpecificDamage(ResourceType resourceType)
    {
        return resourceType == ResourceType.ACTIVE_FIRE
            || resourceType == ResourceType.PIPE_REPAIR
            || resourceType == ResourceType.LUMBER_REPAIR
            || resourceType == ResourceType.DAMAGE_REPAIR
            || resourceType == ResourceType.BOILER_EMPTY
            || resourceType == ResourceType.BOILER_UNLIT;
    }

    private boolean isCategoryEnabled(ResourceType resourceType)
    {
        switch (resourceType)
        {
            case WATER:
                return config.showWater();
            case COLOURED_WATER:
                return config.showColouredWater();
            case HOPPERS:
                return config.showHoppers();
            case BOILER_FUEL:
            case BOILER_EMPTY:
            case BOILER_UNLIT:
            case BOILER_ACTIVE:
                return config.showBoilerFuel();
            case BARK:
                return config.showBark();
            case PROCESSED_BARK:
                return config.showProcessedBark();
            case BAIT:
                return config.showBait();
            case GRUBS:
                return config.showGrubs();
            case FLOWERS:
                return config.showFlowers();
            case BITTERNUTS:
                return config.showBitternuts();
            case BITTERNUTS_FINAL:
                return config.showBitternutsFinal();
            case ACTIVE_FIRE:
            case PIPE_REPAIR:
            case LUMBER_REPAIR:
            case DAMAGE_REPAIR:
                return config.showDamageRepair();
            case CONVEYOR:
            case RUM:
                return config.showRum();
            default:
                return false;
        }
    }

    private boolean isFlashing(ResourceType resourceType)
    {
        switch (resourceType)
        {
            case WATER:
                return config.flashWater();
            case COLOURED_WATER:
                return config.flashColouredWater();
            case HOPPERS:
                return config.flashHoppers();
            case BOILER_FUEL:
            case BOILER_ACTIVE:
                return false;
            case BOILER_EMPTY:
            case BOILER_UNLIT:
                return config.flashBoilerFuel();
            case BARK:
                return config.flashBark();
            case PROCESSED_BARK:
                return config.flashProcessedBark();
            case BAIT:
                return config.flashBait();
            case GRUBS:
                return config.flashGrubs();
            case FLOWERS:
                return config.flashFlowers();
            case BITTERNUTS:
                return config.flashBitternuts();
            case BITTERNUTS_FINAL:
                return config.flashBitternutsFinal();
            case ACTIVE_FIRE:
            case PIPE_REPAIR:
            case LUMBER_REPAIR:
            case DAMAGE_REPAIR:
                return config.flashDamageRepair();
            case CONVEYOR:
                return config.flashConveyor();
            case RUM:
                return config.flashRum();
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
            case BOILER_FUEL:
            case BOILER_EMPTY:
            case BOILER_UNLIT:
            case BOILER_ACTIVE:
                return config.boilerFuelColour();
            case BARK:
                return config.barkColor();
            case PROCESSED_BARK:
                return config.processedBarkColor();
            case BAIT:
                return config.baitColor();
            case GRUBS:
                return config.grubColor();
            case FLOWERS:
                return config.flowerColor();
            case BITTERNUTS:
                return config.bitternutColor();
            case BITTERNUTS_FINAL:
                return config.bitternutFinalColor();
            case PIPE_REPAIR:
                return config.waterColor();
            case LUMBER_REPAIR:
                return config.hopperColor();
            case ACTIVE_FIRE:
            case DAMAGE_REPAIR:
                return config.damageRepairColour();
            case CONVEYOR:
                return config.conveyorColor();
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
