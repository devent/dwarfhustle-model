package com.anrisoftware.dwarfhustle.model.api.objects;

import java.io.Serializable;
import java.util.Optional;

import lombok.Data;

/**
 * Rectangular map area from the north-west corner to the south-east corner.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
public class MapArea implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Creates {@link MapArea} from the north-west corner to the south-east corner.
     */
    public static MapArea create(float nwlat, float nwlon, float selat, float selon) {
        return new MapArea(new MapCoordinate(nwlat, nwlon), new MapCoordinate(selat, selon));
    }

    private static final float PI = (float) Math.PI;

    /**
     * Returns the center of the given {@link MapCoordinate} coordinates. Taken from
     * {@link https://stackoverflow.com/questions/6671183/calculate-the-center-point-of-multiple-latitude-longitude-coordinate-pairs}
     */
    public static MapCoordinate getCentralGeoCoordinate(MapCoordinate... cs) {
        if (cs.length == 1) {
            return cs[0];
        }
        float x = 0;
        float y = 0;
        float z = 0;
        for (var c : cs) {
            var latitude = c.lat * PI / 180f;
            var longitude = c.lon * PI / 180f;
            x += Math.cos(latitude) * Math.cos(longitude);
            y += Math.cos(latitude) * Math.sin(longitude);
            z += Math.sin(latitude);
        }
        var total = cs.length;
        x = x / total;
        y = y / total;
        z = z / total;
        float centralLongitude = (float) Math.atan2(y, x);
        float centralSquareRoot = (float) Math.sqrt(x * x + y * y);
        float centralLatitude = (float) Math.atan2(z, centralSquareRoot);
        return new MapCoordinate(centralLatitude * 180f / PI, centralLongitude * 180f / PI);
    }

    /**
     * North-west corner.
     */
    public final MapCoordinate nw;

    /**
     * South-east corner.
     */
    public final MapCoordinate se;

    private Optional<MapCoordinate> center = Optional.empty();

    /**
     * Returns the center of the area.
     */
    public MapCoordinate getCenter() {
        return center.orElseGet(() -> {
            center = Optional.of(getCentralGeoCoordinate(nw, se));
            return center.get();
        });
    }
}
