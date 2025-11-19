package io.vestoria.enums;

public enum ItemTier {
    LOW(0.5), MEDIUM(1.0), HIGH(1.5), SCARCE(2.0);

    public final Double value;

    ItemTier(Double value) {
        this.value = value;
    }

    public static ItemTier fromString(String s) {
        if (s == null) return null;
        String itemTier = s.trim().toUpperCase();
        try {
            return ItemTier.valueOf(itemTier);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
