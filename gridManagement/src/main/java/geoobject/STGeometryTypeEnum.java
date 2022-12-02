package geoobject;

/**
 * Enumerate of geometry type.
 */
public enum STGeometryTypeEnum {
    UNKNOWN((byte) 0),
    POINT((byte) 1),
    MULTI_POINT((byte) 2),
    TRAJECTORY((byte) 3),
    POLYLINE((byte) 4),
    MULTI_POLYLINE((byte) 5),
    POLYGON((byte) 6),
    MULTI_POLYGON((byte) 7),
    GEOMETRY_COLLECTION((byte) 8);

    private final byte type;

    STGeometryTypeEnum(byte type) {
        this.type = type;
    }

    public static STGeometryTypeEnum fromByte(byte type) {
        switch (type) {
            case 0:
                return UNKNOWN;
            case 1:
                return POINT;
            case 2:
                return MULTI_POINT;
            case 3:
                return TRAJECTORY;
            case 4:
                return POLYLINE;
            case 5:
                return MULTI_POLYLINE;
            case 6:
                return POLYGON;
            case 7:
                return MULTI_POLYGON;
            case 8:
                return GEOMETRY_COLLECTION;
            default:
                return null;
        }
    }

    public static STGeometryTypeEnum fromString(String type) {
        switch (type) {
            case "UNKNOWN":
                return UNKNOWN;
            case "POINT":
                return POINT;
            case "MULTI_POINT":
                return MULTI_POINT;
            case "TRAJECTORY":
                return TRAJECTORY;
            case "POLYLINE":
                return POLYLINE;
            case "MULTI_POLYLINE":
                return MULTI_POLYLINE;
            case "POLYGON":
                return POLYGON;
            case "MULTI_POLYGON":
                return MULTI_POLYGON;
            case "GEOMETRY_COLLECTION":
                return GEOMETRY_COLLECTION;
            default:
                return null;
        }
    }

    public static boolean contains(String type) {
        for (STGeometryTypeEnum t : STGeometryTypeEnum.values()) {
            if (t.name().equals(type)) {
                return true;
            }
        }
        return false;
    }

    public byte getType() {
        return this.type;
    }
}