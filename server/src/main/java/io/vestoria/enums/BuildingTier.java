package io.vestoria.enums;

public enum BuildingTier {
    SMALL(1), MEDIUM(2), LARGE(3);

    public final int value;

    BuildingTier(int value) {
        this.value = value;
    }

    public static BuildingTier fromValue(int value) {
        if (value < 0) {
            return BuildingTier.SMALL;
        }
        if (value > 3) {
            return BuildingTier.LARGE;
        }
        for (BuildingTier tier : BuildingTier.values()) {
            if (tier.value == value) {
                return tier;
            }
        }
        throw new IllegalArgumentException("Bilinmeyen seviye: " + value);
    }
}
