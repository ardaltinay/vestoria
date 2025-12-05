package io.vestoria.enums;

public enum BuildingType {
    SHOP, GARDEN, FARM, FACTORY, MINE;

    public static BuildingType fromString(String s) {
        if (s == null)
            return null;
        String buildingType = s.trim().toUpperCase();
        try {
            return BuildingType.valueOf(buildingType);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
