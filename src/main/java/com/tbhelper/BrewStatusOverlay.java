package com.tbhelper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/** Plain-language production guidance for players learning Trouble Brewing. */
public class BrewStatusOverlay extends OverlayPanel
{
    private static final int PANEL_WIDTH = 235;
    private static final Color HEADER_COLOR = new Color(255, 180, 45);
    private static final Color SECTION_COLOR = new Color(105, 210, 255);
    private static final Color GOOD_COLOR = new Color(85, 220, 100);
    private static final Color WARNING_COLOR = new Color(255, 145, 35);
    private static final Color BAD_COLOR = new Color(235, 80, 80);
    private static final Color MUTED_COLOR = new Color(185, 185, 185);

    private final TroubleBrewingHighlighterPlugin plugin;
    private final TroubleBrewingHighlighterConfig config;

    @Inject
    private BrewStatusOverlay(
        TroubleBrewingHighlighterPlugin plugin,
        TroubleBrewingHighlighterConfig config)
    {
        super(plugin);
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.TOP_RIGHT);
        setPriority(OverlayPriority.LOW);
        setMovable(true);
        setSnappable(true);
        panelComponent.setPreferredSize(new Dimension(PANEL_WIDTH, 0));
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.showBrewStatus() || !plugin.isTroubleBrewingMatchActive())
        {
            return null;
        }

        panelComponent.getChildren().clear();
        panelComponent.getChildren().add(
            TitleComponent.builder()
                .text("Brew Status")
                .color(HEADER_COLOR)
                .build()
        );

        String nextAction = plugin.getBrewStatusAction();
        panelComponent.getChildren().add(
            LineComponent.builder()
                .left("DO THIS NEXT")
                .leftColor(HEADER_COLOR)
                .right(nextAction)
                .rightColor(actionColor(nextAction))
                .build()
        );

        int remainingRums = plugin.getRemainingRums();
        addSection("SUPPLIES FOR " + remainingRums + " RUM");
        addSupply("Bitternuts", plugin.getTeamBitternuts(), remainingRums);
        addSupply("Sweetgrubs", plugin.getTeamSweetgrubs(), remainingRums);
        addSupply("Water buckets", plugin.getTeamBuckets(), remainingRums * 5);
        addSupply("Coloured water", plugin.getTeamColouredWater(), remainingRums * 3);
        addSupply("Scrapey bark", plugin.getTeamBark(), remainingRums);

        addSection("PRODUCTION");
        int lowestBoilerLogs = plugin.getLowestBoilerLogCount();
        addLine(
            "Lowest boiler",
            lowestBoilerLogs + " / 10 logs",
            lowestBoilerLogs < 10 ? WARNING_COLOR : GOOD_COLOR
        );

        int cycleSeconds = plugin.getBrewCycleSecondsRemaining();
        String rumState = plugin.isRumReady()
            ? "Ready"
            : cycleSeconds >= 0 ? "Brewing" : "Waiting";
        addLine(
            "Rum output",
            rumState,
            plugin.isRumReady() ? GOOD_COLOR : cycleSeconds >= 0 ? SECTION_COLOR : MUTED_COLOR
        );
        if (plugin.getRumLoadsAvailable() == 0)
        {
            addLine("Unlock collecting rum", "Add ingredients", WARNING_COLOR);
        }
        addLine(
            "Cycle",
            cycleSeconds < 0 ? "-" : cycleSeconds == 0 ? "Finishing" : "~" + cycleSeconds + " sec",
            cycleSeconds >= 0 ? SECTION_COLOR : MUTED_COLOR
        );
        addLine("Time left", compactMatchTime(plugin.getMatchTime()), Color.WHITE);

        return super.render(graphics);
    }

    private void addSection(String text)
    {
        panelComponent.getChildren().add(
            LineComponent.builder()
                .left(text)
                .leftColor(SECTION_COLOR)
                .build()
        );
    }

    private void addSupply(String label, int current, int required)
    {
        addLine(
            label,
            current + " / " + required,
            supplyColour(current, required)
        );
    }

    private static Color supplyColour(int current, int required)
    {
        if (required <= 0 || current >= required)
        {
            return GOOD_COLOR;
        }
        return current <= 0 ? BAD_COLOR : WARNING_COLOR;
    }

    private void addLine(String label, String value, Color valueColor)
    {
        panelComponent.getChildren().add(
            LineComponent.builder()
                .left(label)
                .right(value)
                .rightColor(valueColor)
                .build()
        );
    }

    private static Color actionColor(String action)
    {
        if ("Collect rum".equals(action) || "Keep supplies flowing".equals(action))
        {
            return GOOD_COLOR;
        }
        if ("Brewing".equals(action))
        {
            return SECTION_COLOR;
        }
        return WARNING_COLOR;
    }

    private static String compactMatchTime(String matchTime)
    {
        String prefix = "Time Left:";
        return matchTime.startsWith(prefix)
            ? matchTime.substring(prefix.length()).trim()
            : matchTime;
    }
}
