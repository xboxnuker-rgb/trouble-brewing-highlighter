package com.tbhelper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import javax.inject.Inject;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/** Passively outlines the recommended Trouble Brewing monkey dialogue option. */
public class MonkeyDialogueOverlay extends Overlay
{
    private final TroubleBrewingHighlighterPlugin plugin;
    private final TroubleBrewingHighlighterConfig config;

    @Inject
    private MonkeyDialogueOverlay(
        TroubleBrewingHighlighterPlugin plugin,
        TroubleBrewingHighlighterConfig config)
    {
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.showMonkeyDialogueHelper())
        {
            return null;
        }

        Widget choice = plugin.getRecommendedMonkeyChoice();
        if (choice == null)
        {
            return null;
        }

        Rectangle bounds = choice.getBounds();
        if (bounds == null)
        {
            return null;
        }

        Color oldColor = graphics.getColor();
        Stroke oldStroke = graphics.getStroke();
        Color colour = config.monkeyDialogueColor();

        graphics.setStroke(new BasicStroke((float) config.outlineWidth()));
        graphics.setColor(new Color(
            colour.getRed(),
            colour.getGreen(),
            colour.getBlue(),
            config.fillOpacity()
        ));
        graphics.fill(bounds);
        graphics.setColor(colour);
        graphics.draw(bounds);

        graphics.setColor(oldColor);
        graphics.setStroke(oldStroke);
        return null;
    }
}
