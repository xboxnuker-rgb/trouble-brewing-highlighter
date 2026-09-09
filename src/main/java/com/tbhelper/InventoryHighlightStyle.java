package com.tbhelper;

public enum InventoryHighlightStyle
{
    ITEM_OUTLINE("Item outline"),
    FULL_SLOT("Full slot"),
    OFF("Off");

    private final String displayName;

    InventoryHighlightStyle(String displayName)
    {
        this.displayName = displayName;
    }

    @Override
    public String toString()
    {
        return displayName;
    }
}
