package com.tbhelper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.WidgetItemOverlay;
import net.runelite.client.util.ImageUtil;

/** Draws the same category colours over matching Trouble Brewing inventory items. */
public class TroubleBrewingInventoryOverlay extends WidgetItemOverlay
{
    private static final long FLASH_INTERVAL_MS = 500L;
    private static final float SUPPLY_BADGE_FONT_SIZE = 13F;
    private static final int SUPPLY_BADGE_PADDING_X = 3;
    private static final Color SUPPLY_BADGE_BACKGROUND = new Color(0, 0, 0, 190);
    private static final Color SUPPLY_READY_COLOR = new Color(85, 220, 100);
    private static final Color SUPPLY_PARTIAL_COLOR = new Color(255, 145, 35);
    private static final Color SUPPLY_EMPTY_COLOR = new Color(235, 80, 80);
    private static final int ITEM_OUTLINE_CACHE_SIZE = 128;
    private final Client client;
    private final ItemManager itemManager;
    private final TroubleBrewingHighlighterPlugin plugin;
    private final TroubleBrewingHighlighterConfig config;
    private final Map<ItemOutlineKey, BufferedImage> itemOutlineCache =
        new LinkedHashMap<ItemOutlineKey, BufferedImage>(ITEM_OUTLINE_CACHE_SIZE, 0.75F, true)
        {
            @Override
            protected boolean removeEldestEntry(Map.Entry<ItemOutlineKey, BufferedImage> eldest)
            {
                return size() > ITEM_OUTLINE_CACHE_SIZE;
            }
        };

    @Inject
    private TroubleBrewingInventoryOverlay(
        Client client,
        ItemManager itemManager,
        TroubleBrewingHighlighterPlugin plugin,
        TroubleBrewingHighlighterConfig config)
    {
        this.client = client;
        this.itemManager = itemManager;
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
        renderToolWidget(graphics, InterfaceID.BrewTools.BAMBOO_MODEL1, ResourceType.PIPE_REPAIR);
        renderToolWidget(graphics, InterfaceID.BrewTools.BRIDGE_MODEL1, ResourceType.DAMAGE_REPAIR);
        renderToolWidget(graphics, InterfaceID.BrewTools.PATCH_MODEL1, ResourceType.LUMBER_REPAIR);
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

        InventoryHighlightStyle highlightStyle = config.inventoryHighlightStyle();
        if (highlightStyle == InventoryHighlightStyle.OFF)
        {
            return;
        }

        ResourceType resourceType = ObjectDatabase.getItem(itemId);
        if ((itemId == ItemID.LOGS && !plugin.needsBoilerLogs())
            || (itemId == ItemID.TINDERBOX && !plugin.needsBoilerLighting()))
        {
            return;
        }

        boolean emergencyWater = itemId == ItemID.BUCKET_WATER
            && plugin.hasLocalTeamFire()
            && config.showDamageRepair();
        if (resourceType == null || (!emergencyWater && !isCategoryEnabled(resourceType)))
        {
            return;
        }

        Rectangle bounds = widgetItem.getCanvasBounds();
        if (bounds == null)
        {
            return;
        }

        if (!isFlashing(resourceType, emergencyWater) || isFlashOn())
        {
            Color colour = emergencyWater
                ? config.damageRepairColour()
                : colourFor(resourceType);
            if (highlightStyle == InventoryHighlightStyle.ITEM_OUTLINE)
            {
                renderItemOutline(
                    graphics,
                    bounds,
                    itemId,
                    widgetItem.getQuantity(),
                    colour
                );
            }
            else
            {
                renderBounds(graphics, bounds, colour);
            }
        }
        renderSupplyBadge(graphics, bounds, itemId, resourceType);
    }

    private void renderSupplyBadge(
        Graphics2D graphics,
        Rectangle bounds,
        int itemId,
        ResourceType resourceType)
    {
        if (!plugin.isTroubleBrewingMatchActive())
        {
            return;
        }

        boolean waterBucket = isWaterBucket(itemId);
        boolean showBadge = shouldShowSupplyBadge(
            config.afkerMode(),
            waterBucket,
            config.showWaterBucketCount(),
            config.showStationAmounts()
        );
        if (!showBadge)
        {
            return;
        }

        int current;
        int perRum;
        if (itemId == ItemID.BREW_MONKEY)
        {
            // Preserve the plain monkey's route colour, but show the supply it
            // is used to collect rather than the coloured-water total.
            current = plugin.getTeamBitternuts();
            perRum = 1;
        }
        else
        {
            switch (resourceType)
            {
                case WATER:
                    current = plugin.getTeamBuckets();
                    perRum = 5;
                    break;
                case COLOURED_WATER:
                case FLOWERS:
                    current = plugin.getTeamColouredWater();
                    perRum = 3;
                    break;
                case BARK:
                    if (itemId == ItemID.BRONZE_AXE || itemId == ItemID.KNIFE)
                    {
                        return;
                    }
                    current = plugin.getTeamBark();
                    perRum = 1;
                    break;
                case PROCESSED_BARK:
                    current = plugin.getTeamBark();
                    perRum = 1;
                    break;
                case BAIT:
                case GRUBS:
                    current = plugin.getTeamSweetgrubs();
                    perRum = 1;
                    break;
                case BITTERNUTS:
                case BITTERNUTS_FINAL:
                    current = plugin.getTeamBitternuts();
                    perRum = 1;
                    break;
                default:
                    return;
            }
        }

        int required = plugin.getPossibleRumsLeft() * perRum;
        String text = current + "/" + required;
        Font oldFont = graphics.getFont();
        Color oldColor = graphics.getColor();
        graphics.setFont(oldFont.deriveFont(Font.BOLD, SUPPLY_BADGE_FONT_SIZE));
        int textWidth = graphics.getFontMetrics().stringWidth(text);
        int textX = bounds.x + ((bounds.width - textWidth) / 2);
        int textY = bounds.y + graphics.getFontMetrics().getAscent() + 1;
        int backgroundX = textX - SUPPLY_BADGE_PADDING_X;
        int backgroundWidth = textWidth + (SUPPLY_BADGE_PADDING_X * 2);
        int backgroundHeight = graphics.getFontMetrics().getHeight() + 1;
        graphics.setColor(SUPPLY_BADGE_BACKGROUND);
        graphics.fillRoundRect(
            backgroundX,
            bounds.y,
            backgroundWidth,
            backgroundHeight,
            4,
            4
        );
        graphics.setColor(Color.BLACK);
        graphics.drawString(text, textX - 1, textY);
        graphics.drawString(text, textX + 1, textY);
        graphics.drawString(text, textX, textY - 1);
        graphics.drawString(text, textX, textY + 1);
        graphics.setColor(supplyColour(current, required));
        graphics.drawString(text, textX, textY);
        graphics.setFont(oldFont);
        graphics.setColor(oldColor);
    }

    private static Color supplyColour(int current, int required)
    {
        if (required <= 0 || current >= required)
        {
            return SUPPLY_READY_COLOR;
        }
        return current <= 0 ? SUPPLY_EMPTY_COLOR : SUPPLY_PARTIAL_COLOR;
    }

    private void renderToolWidget(Graphics2D graphics, int componentId, ResourceType resourceType)
    {
        if (!isEnabled(resourceType, false))
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

    private void renderItemOutline(
        Graphics2D graphics,
        Rectangle bounds,
        int itemId,
        int quantity,
        Color colour)
    {
        int size = Math.max(1, Math.min(config.inventoryOutlineSize(), 9));
        ItemOutlineKey key = new ItemOutlineKey(itemId, quantity, colour.getRGB(), size);
        BufferedImage outline = itemOutlineCache.get(key);
        if (outline == null)
        {
            outline = itemManager.getItemOutline(itemId, quantity, colour);
            if (outline == null)
            {
                return;
            }

            int additionalFullLayers = (size - 1) / 4;
            for (int layer = 0; layer < additionalFullLayers; layer++)
            {
                outline = ImageUtil.outlineImage(outline, colour, true);
            }

            int partialLayer = (size - 1) % 4;
            if (partialLayer > 0)
            {
                Color partialColour = new Color(
                    colour.getRed(),
                    colour.getGreen(),
                    colour.getBlue(),
                    partialLayer * 64
                );
                outline = ImageUtil.outlineImage(outline, partialColour, true);
            }
            itemOutlineCache.put(key, outline);
        }

        graphics.drawImage(outline, bounds.x, bounds.y, null);
    }

    private static boolean isWaterBucket(int itemId)
    {
        return itemId == ItemID.BUCKET_EMPTY
            || itemId == ItemID.BREW_BUCKET_DUMMY
            || itemId == ItemID.BUCKET_WATER;
    }

    static boolean shouldShowSupplyBadge(
        boolean afkerMode,
        boolean waterBucket,
        boolean showWaterBucketCount,
        boolean showStationAmounts)
    {
        return afkerMode
            ? waterBucket && showWaterBucketCount
            : showStationAmounts;
    }

    private boolean isFlashOn()
    {
        return (System.currentTimeMillis() / FLASH_INTERVAL_MS) % 2L == 0L;
    }

    private boolean isEnabled(ResourceType resourceType, boolean emergencyWater)
    {
        return isCategoryEnabled(resourceType)
            && (!isFlashing(resourceType, emergencyWater) || isFlashOn());
    }

    private boolean isCategoryEnabled(ResourceType resourceType)
    {
        return plugin.isResourceHighlightEnabled(resourceType);
    }

    private boolean isFlashing(ResourceType resourceType, boolean emergencyWater)
    {
        if (emergencyWater)
        {
            return config.flashDamageRepair();
        }

        switch (resourceType)
        {
            case WATER:
                return config.flashWater();
            case COLOURED_WATER:
                return config.flashColouredWater();
            case HOPPERS:
                return config.flashHoppers();
            case BOILER_FUEL:
            case BOILER_EMPTY:
            case BOILER_UNLIT:
                return config.flashBoilerFuel();
            case BOILER_ACTIVE:
                return false;
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
                return config.flashDamageRepair();
            case PIPE_REPAIR:
            case LUMBER_REPAIR:
            case DAMAGE_REPAIR:
                return config.flashDamageRepair()
                    && (plugin.getLocalTeamRepairParts(resourceType) > 0
                        || plugin.getLocalTeamObjectCount(resourceType) > 0);
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

    private static final class ItemOutlineKey
    {
        private final int itemId;
        private final int quantity;
        private final int colour;
        private final int size;

        private ItemOutlineKey(int itemId, int quantity, int colour, int size)
        {
            this.itemId = itemId;
            this.quantity = quantity;
            this.colour = colour;
            this.size = size;
        }

        @Override
        public boolean equals(Object object)
        {
            if (this == object)
            {
                return true;
            }
            if (!(object instanceof ItemOutlineKey))
            {
                return false;
            }

            ItemOutlineKey other = (ItemOutlineKey) object;
            return itemId == other.itemId
                && quantity == other.quantity
                && colour == other.colour
                && size == other.size;
        }

        @Override
        public int hashCode()
        {
            int result = itemId;
            result = 31 * result + quantity;
            result = 31 * result + colour;
            return 31 * result + size;
        }
    }
}
