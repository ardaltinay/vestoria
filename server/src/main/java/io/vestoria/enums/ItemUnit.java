package io.vestoria.enums;

public enum ItemUnit {
    PIECE, KG, METER, LITER;

    public static ItemUnit fromString(String s) {
        if (s == null)
            return null;
        String itemUnit = s.trim().toUpperCase();
        try {
            return ItemUnit.valueOf(itemUnit);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
