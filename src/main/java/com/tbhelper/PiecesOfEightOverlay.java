package com.tbhelper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;

/** Displays the cached Pieces of Eight total near Trouble Brewing. */
public class PiecesOfEightOverlay extends OverlayPanel
{
    private static final Color PIECES_COLOR = new Color(255, 190, 0);
    private static final Color INCOMPLETE_CONTRIBUTION_COLOR = new Color(255, 0, 0);
    private static final Color COMPLETE_CONTRIBUTION_COLOR = new Color(0, 255, 0);
    private final TroubleBrewingHighlighterPlugin plugin;
    private final TroubleBrewingHighlighterConfig config;

    @Inject
    private PiecesOfEightOverlay(
        TroubleBrewingHighlighterPlugin plugin,
        TroubleBrewingHighlighterConfig config)
    {
        super(plugin);
        this.plugin = plugin;
        this.config = config;

        // RuneLite promotes freely positioned UNDER_WIDGETS overlays above
        // interfaces. ABOVE_SCENE preserves Alt-dragging while allowing game
        // interfaces to cover this panel.
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.LOW);
        setMovable(true);
        setSnappable(true);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.showPiecesOfEight() || !plugin.isTroubleBrewingVicinity())
        {
            return null;
        }

        panelComponent.getChildren().clear();
        panelComponent.getChildren().add(
            LineComponent.builder()
                .left("Pieces of Eight")
                .right(Integer.toString(plugin.getPiecesOfEight()))
                .rightColor(PIECES_COLOR)
                .build()
        );
        if (plugin.isTroubleBrewingMatchActive())
        {
            int contribution = plugin.getPlayerContribution();
            panelComponent.getChildren().add(
                LineComponent.builder()
                    .left("Contribution")
                    .right(Integer.toString(contribution))
                    .rightColor(contribution >= 100
                        ? COMPLETE_CONTRIBUTION_COLOR
                        : INCOMPLETE_CONTRIBUTION_COLOR)
                    .build()
            );
            panelComponent.getChildren().add(
                LineComponent.builder()
                    .left("Expected")
                    .right(Integer.toString(plugin.getExpectedPiecesOfEight()))
                    .rightColor(PIECES_COLOR)
                    .build()
            );
        }
        return super.render(graphics);
    }
}
