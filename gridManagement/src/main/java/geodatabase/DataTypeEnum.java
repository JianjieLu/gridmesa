package geodatabase;

public enum DataTypeEnum {
    /**
     * Spatial temporal objects, such as point, line and area
     */
    STFEATURE(0, "STFeature"),

    /**
     * Dynamic Geographic object, i.e. a series of trajectory points
     */
    DGOBJECT(1, "DGObject"),

    /**
     * Temporal spatial tile
     */
    TSTILE(2, "TSTile");

    private int index;
    private String name;

    DataTypeEnum(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public static DataTypeEnum getDataTypeEnum(String name) {
        switch (name) {
            case "STFeature":
                return STFEATURE;
            case "DGObject":
                return DGOBJECT;
            case "TSTile":
                return TSTILE;
            default:
                throw new UnsupportedOperationException("Unsupported Data type name");
        }
    }

    @Override
    public String toString() {
        return "index: " + this.index + ", name: " + this.name;
    }
}
