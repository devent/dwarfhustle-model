/*
 * dwarfhustle-model-api - Manages the compile dependencies for the model.
 * Copyright © 2022-2025 Erwin Müller (erwin.mueller@anrisoftware.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.anrisoftware.dwarfhustle.model.api.objects;

import java.awt.Color;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Range;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import org.shredzone.commons.suncalc.SunPosition;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 *
 * Updates the position, directional and ambient light color based on the time
 * and location.
 *
 * @author Erwin Müller
 */
@ToString(onlyExplicitlyIncluded = true)
public class SunModel {

    private static abstract class ColorCalc {

        public abstract float[] getColor(float angle);

    }

    private static class InterpNop extends ColorCalc {

        private float[] c = new float[3];

        public InterpNop(Color color) {
            this.c = color.getColorComponents(null);
        }

        @Override
        public float[] getColor(float angle) {
            return c;
        }
    }

    @RequiredArgsConstructor
    private static class InterpStartEndColors extends ColorCalc {

        private float[] c = new float[3];

        private final PolynomialSplineFunction[] f = new PolynomialSplineFunction[3];

        private final float min;

        private final float[] cstart;

        private final float[] cend;

        private final float max;

        public InterpStartEndColors(float min, float max, Color startColor, Color endColor) {
            this.min = min;
            this.max = max;
            var x = new double[] { min, max };
            this.cstart = new float[3];
            startColor.getColorComponents(cstart);
            this.cend = new float[3];
            endColor.getColorComponents(cend);
            for (int i = 0; i < c.length; i++) {
                this.f[i] = new LinearInterpolator().interpolate(x, new double[] { cstart[i], cend[i] });
            }
        }

        @Override
        public float[] getColor(float angle) {
            if (angle > max) {
                return cend;
            } else if (angle > min) {
                for (int i = 0; i < c.length; i++) {
                    c[i] = (float) f[i].value(angle);
                }
                return c;
            } else {
                return cstart;
            }
        }
    }

    private final List<Pair<Range<Float>, ColorCalc>> anglesColors;

    @ToString.Include
    public float x;

    @ToString.Include
    public float y;

    @ToString.Include
    public float z;

    @ToString.Include
    public float[] color;

    @ToString.Include
    public final float[] ambientColor = new float[3];

    @ToString.Include
    public boolean visible;

    private float altitude;

    private final Range<Float> rdawn;

    private final Range<Float> rdaylight;

    private final Range<Float> rnight;

    public SunModel() {
        this.anglesColors = new ArrayList<>();
        var day = new float[3];
        BlackBodyColor.temp_to_rgb(7000, day);
        rnight = Range.of(-90f, -5f);
        var cnight = new InterpNop(new Color(38, 38, 128));
        rdawn = Range.of(-5f, 0f);
        var cdawn = new InterpStartEndColors(rdawn.getMinimum(), rdawn.getMaximum(), new Color(0, 0, 255),
                new Color(200, 200, 255));
        rdaylight = Range.of(0f, 90f);
        var cdaylight = new InterpStartEndColors(rdaylight.getMinimum(), 15f, new Color(200, 200, 255),
                new Color(255, 255, 255));
        anglesColors.add(Tuples.pair(rnight, cnight));
        anglesColors.add(Tuples.pair(rdawn, cdawn));
        anglesColors.add(Tuples.pair(rdaylight, cdaylight));
    }

    /**
     * Updates the position, directional and ambient light color based on the time
     * and location.
     */
    public void update(ZonedDateTime time, float lat, float lng) {
        updatePosition(time, lat, lng);
        updateColor(time, lat, lng);
        updateAmbientColor();
    }

    private void updateColor(ZonedDateTime time, float lat, float lng) {
        ColorCalc ccalc = null;
        for (int i = 0; i < anglesColors.size(); i++) {
            var pair = anglesColors.get(i);
            if (pair.getOne().contains(altitude)) {
                ccalc = pair.getTwo();
            }
        }
        this.color = ccalc.getColor(altitude);
    }

    private void updatePosition(ZonedDateTime time, float lat, float lng) {
        var pos = SunPosition.compute().on(time).at(lat, lng).execute();
        var r = 100.0;
        var alt = pos.getAltitude();
        this.altitude = (float) alt;
        var al = Math.toRadians(alt);
        var az = Math.toRadians(pos.getAzimuth());
        // x = r .* cos(elevation) .* cos(azimuth)
        // y = r .* cos(elevation) .* sin(azimuth)
        // z = r .* sin(elevation)
        this.y = (float) (r * Math.cos(al) * Math.cos(az));
        this.x = (float) (r * Math.cos(al) * Math.sin(az));
        this.z = (float) (r * Math.sin(al));
        this.visible = altitude > 0.1f;
    }

    private void updateAmbientColor() {
        if (rdaylight.contains(altitude)) {
            ambientColor[0] = 1.0f;
            ambientColor[1] = 1.0f;
            ambientColor[2] = 1.0f;
        } else if (rdawn.contains(altitude)) {
            ambientColor[0] = 0.8f;
            ambientColor[1] = 0.8f;
            ambientColor[2] = 0.8f;
        } else {
            ambientColor[0] = 0.5f;
            ambientColor[1] = 0.5f;
            ambientColor[2] = 0.5f;
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public float getY() {
        return y;
    }

    public float getX() {
        return x;
    }

    public float getZ() {
        return z;
    }

    public float[] getColor() {
        return color;
    }

    public float[] getAmbientColor() {
        return ambientColor;
    }

}
