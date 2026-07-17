package com.tbhelper;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TroubleBrewingHighlighterPluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(TroubleBrewingHighlighterPlugin.class);
        RuneLite.main(args);
    }
}