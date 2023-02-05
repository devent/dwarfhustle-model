package com.anrisoftware.dwarfhustle.model.api.objects;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import lombok.Data;

/**
 * Map coordinate of latitude and longitude.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
@Data
public class MapCoordinate implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The minimum allowed latitude
	 */
	public static float MIN_LATITUDE = Float.valueOf("-90.0000");

	/**
	 * The maximum allowed latitude
	 */
	public static float MAX_LATITUDE = Float.valueOf("90.0000");

	/**
	 * The minimum allowed longitude
	 */
	public static float MIN_LONGITUDE = Float.valueOf("-180.0000");

	/**
	 * The maximum allowed longitude
	 */
	public static float MAX_LONGITUDE = Float.valueOf("180.0000");

	/**
	 * Converts degrees, minutes, seconds to decimal degrees.
	 */
	public static float toDecimalDegrees(float d, float m, float s) {
		return d + m / 60f + s / 3600f;
	}

	/**
	 * Converts decimal degrees to degrees, minutes and seconds.
	 */
	public static float[] toDegreesMinSec(float dd, float[] storage) {
		if (storage == null) {
			storage = new float[3];
		}
		float d = (int) dd;
		float m = (int) ((dd - d) * 60f);
		float s = (int) ((dd - d - m / 60f) * 3600f);
		storage[0] = Math.abs(d);
		storage[1] = Math.abs(m);
		storage[2] = Math.abs(s);
		return storage;
	}

	private static final NumberFormat TO_STRING_FORMATTER = DecimalFormat.getInstance(Locale.US);

	public final float lat;

	public final float lon;

	public MapCoordinate(float lat, float lon) {
		if (lat > MAX_LATITUDE || lat < MIN_LATITUDE || lon > MAX_LONGITUDE || lon < MIN_LONGITUDE) {
			throw new IllegalArgumentException();
		}
		this.lat = lat;
		this.lon = lon;
	}

	@Override
	public String toString() {
		var llat = toDegreesMinSec(lat, null);
		var dlat = lat >= 0 ? "N" : "S";
		var llon = toDegreesMinSec(lon, null);
		var dlon = lon >= 0 ? "E" : "W";
		return s(llat, 0) + "°" + s(llat, 1) + "'" + s(llat, 2) + "\"" + dlat + "," + //
				s(llon, 0) + "°" + s(llon, 1) + "'" + s(llon, 2) + "\"" + dlon;
	}

	private String s(float[] llat, int n) {
		return TO_STRING_FORMATTER.format(llat[n]);
	}
}
