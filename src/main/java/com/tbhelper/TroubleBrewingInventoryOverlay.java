package com.tbhelper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

/** Draws the same category colours over matching Trouble Brewing inventory items. */
public class TroubleBrewingInventoryOverlay extends WidgetItemOverlay
{
    private static final long FLASH_INTERVAL_MS = 500L;
    private final Client client;
    private final TroubleBrewingHighlighterPlugin plugin;
    private final TroubleBrewingHighlighterConfig config;

    @Inject
    private TroubleBrewingInventoryOverlay(
        Client client,
        TroubleBrewingHighlighterPlugin plugin,
        TroubleBrewingHighlighterConfig config)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        showOnInventory();
        showOnInterfaces(InterfaceID.BREW_TOOLS);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        Shape oldClip = graphics.getClip();
        Dimension dimension = super.render(graphics);
        graphics.setClip(oldClip);

        if (!plugin.isTroubleBrewingSceneLoaded())
        {
            return dimension;
        }

        renderToolWidget(graphics, InterfaceID.BrewTools.BUCKET_MODEL1, ResourceType.WATER);
        renderToolWidget(graphics, InterfaceID.BrewTools.TINDERBOX_MODEL1, ResourceType.BOILER_FUEL);
        renderToolWidget(graphics, InterfaceID.BrewTools.AXE_MODEL1, ResourceType.BARK);
        renderToolWidget(graphics, InterfaceID.BrewTools.KNIFE_MODEL1, ResourceType.BARK);
        renderToolWidget(graphics, InterfaceID.BrewTools.BOWL_MODEL1, ResourceType.FLOWERS);
        renderToolWidget(graphics, InterfaceID.BrewTools.MEAT_MODEL1, ResourceType.BAIT);
        renderToolWidget(graphics, InterfaceID.BrewTools.BAMBOO_MODEL1, ResourceType.DAMAGE_REPAIR );
        renderToolWidget(graphics, InterfaceID.BrewTools.BRIDGE_MODEL1, ResourceType.DAMAGE_REPAIR);
        renderToolWidget(graphics, InterfaceID.BrewTools.PATCH_MODEL1, ResourceType.DAMAGE_REPAIR);
        return dimension;
    }

    @Override
    public void renderItemOverlay(
        Graphics2D graphics,
        int itemId,
        WidgetItem widgetItem)
    {
        // Generic IDs such as buckets and axes should only be coloured while
        // the Trouble Brewing scene itself is loaded.
        if (!plugin.isTroubleBrewingSceneLoaded())
        {
            return;
        }

        ResourceType resourceType = ObjectDatabase.getItem(itemId);
        if (resourceType == null || !isEnabled(resourceType))
        {
            return;
        }

        Rectangle bounds = widgetItem.getCanvasBounds();
        if (bounds == null)
        {
            return;
        }

        renderBounds(graphics, bounds, colourFor(resourceType));
    }

    private void renderToolWidget(Graphics2D graphics, int componentId, ResourceType resourceType)
    {
        if (!isEnabled(resourceType))
        {
            return;
        }

        Widget widget = client.getWidget(componentId);
        if (widget == null || widget.isHidden())
        {
            return;
        }

        Rectangle bounds = widget.getBounds();
        if (bounds != null)
        {
            renderBounds(graphics, bounds, colourFor(resourceType));
        }
    }

    private void renderBounds(Graphics2D graphics, Rectangle bounds, Color colour)
    {
        Stroke oldStroke = graphics.getStroke();
        Color oldColor = graphics.getColor();

        graphics.setStroke(new BasicStroke((float) config.outlineWidth()));
        graphics.setColor(withAlpha(colour, config.fillOpacity()));
        graphics.fill(bounds);
        graphics.setColor(colour);
        graphics.draw(bounds);

        graphics.setStroke(oldStroke);
        graphics.setColor(oldColor);
    }

    private boolean isFlashOn()
    {
        return (System.currentTimeMillis() / FLASH_INTERVAL_MS) % 2L == 0L;
    }

    private boolean isEnabled(ResourceType resourceType)
    {
        return isCategoryEnabled(resourceType)
            && (!isFlashing(resourceType) || isFlashOn());
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
