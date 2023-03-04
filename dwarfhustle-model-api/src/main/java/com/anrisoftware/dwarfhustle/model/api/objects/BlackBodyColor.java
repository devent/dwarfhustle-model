/**
 * Dwarf Hustle :: Model :: Sun-Moon - Calculates the position of the sun and moon.
 * Copyright © 2020 Erwin Müller (erwin.mueller@anrisoftware.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.anrisoftware.dwarfhustle.model.api.objects;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Color;

/**
 * Calculates the color from the black body temperature.
 *
 * @see http://www.vendian.org/mncharity/dir3/blackbody/UnstableURLs/bbr_color.html
 * @see https://stackoverflow.com/questions/7229895/display-temperature-as-a-color-with-c
 *
 * @author Erwin Müller
 */
public class BlackBodyColor {

    /**
     * Calculates the color approximating the black body temperature.
     *
     * @see https://stackoverflow.com/questions/7229895/display-temperature-as-a-color-with-c/20183566#20183566
     *
     * @param t the temperature in Kelvin.
     *
     * @return the {@link Color}
     */
    public static Color blackBodyColor(float t) {
        float x = t / 1000f;
        float x2 = x * x;
        float x3 = x2 * x;
        float x4 = x3 * x;
        float x5 = x4 * x;

        float R, G, B = 0f;

        // red
        if (t <= 6600) {
            R = 1f;
        } else {
            R = 0.0002889f * x5 - 0.01258f * x4 + 0.2148f * x3 - 1.776f * x2 + 6.907f * x - 8.723f;
        }

        // green
        if (t <= 6600) {
            G = -4.593e-05f * x5 + 0.001424f * x4 - 0.01489f * x3 + 0.0498f * x2 + 0.1669f * x - 0.1653f;
        } else {
            G = -1.308e-07f * x5 + 1.745e-05f * x4 - 0.0009116f * x3 + 0.02348f * x2 - 0.3048f * x + 2.159f;
        }

        // blue
        if (t <= 2000f) {
            B = 0f;
        } else if (t < 6600f) {
            B = 1.764e-05f * x5 + 0.0003575f * x4 - 0.01554f * x3 + 0.1549f * x2 - 0.3682f * x + 0.2386f;
        } else {
            B = 1f;
        }

        return new Color(R, G, B, 1);
    }

    /**
     * Calculates the color approximating the black body temperature.
     *
     * @see https://stackoverflow.com/questions/7229895/display-temperature-as-a-color-with-c/24856307#24856307
     *
     * @param t      the temperature in Kelvin.
     * @param colors
     */
    public static float[] temp_to_rgb(float t, float[] colors) {
        float tx = t / 1000.f;
        float tt;
        float[] coeffs = new float[8];
        // red
        float red;
        if (tx < 6.527) {
            red = 1.0f;
        } else {
            coeffs[0] = 4.93596077e+00f;
            coeffs[1] = -1.29917429e+00f;
            coeffs[2] = 1.64810386e-01f;
            coeffs[3] = -1.16449912e-02f;
            coeffs[4] = 4.86540872e-04f;
            coeffs[5] = -1.19453511e-05f;
            coeffs[6] = 1.59255189e-07f;
            coeffs[7] = -8.89357601e-10f;
            tt = min(tx, 40);
            red = poly(coeffs, tt);
        }
        red = max(red, 0);
        red = min(red, 1);
        // green
        float green;
        if (tx < 0.85) {
            green = 0.0f;
        } else if (tx < 6.6) {
            coeffs[0] = -4.95931720e-01f;
            coeffs[1] = 1.08442658e+00f;
            coeffs[2] = -9.17444217e-01f;
            coeffs[3] = 4.94501179e-01f;
            coeffs[4] = -1.48487675e-01f;
            coeffs[5] = 2.49910386e-02f;
            coeffs[6] = -2.21528530e-03f;
            coeffs[7] = 8.06118266e-05f;
            green = poly(coeffs, tx);
        } else {
            coeffs[0] = 3.06119745e+00f;
            coeffs[1] = -6.76337896e-01f;
            coeffs[2] = 8.28276286e-02f;
            coeffs[3] = -5.72828699e-03f;
            coeffs[4] = 2.35931130e-04f;
            coeffs[5] = -5.73391101e-06f;
            coeffs[6] = 7.58711054e-08f;
            coeffs[7] = -4.21266737e-10f;
            tt = min(tx, 40);
            green = poly(coeffs, tt);
        }
        green = max(green, 0);
        green = min(green, 1);
        // blue
        float blue;
        if (tx < 1.9) {
            blue = 0.0f;
        } else if (tx < 6.6) {
            coeffs[0] = 4.93997706e-01f;
            coeffs[1] = -8.59349314e-01f;
            coeffs[2] = 5.45514949e-01f;
            coeffs[3] = -1.81694167e-01f;
            coeffs[4] = 4.16704799e-02f;
            coeffs[5] = -6.01602324e-03f;
            coeffs[6] = 4.80731598e-04f;
            coeffs[7] = -1.61366693e-05f;
            blue = poly(coeffs, tx);
        } else {
            blue = 1.0f;
        }
        blue = max(blue, 0);
        blue = min(blue, 1);

        colors[0] = red;
        colors[1] = green;
        colors[2] = blue;
        return colors;
    }

    public static float poly(float[] coefficients, float x) {
        float result = coefficients[0];
        float xn = x;
        for (int i = 1; i < coefficients.length; i++) {
            result += xn * coefficients[i];
            xn *= x;

        }
        return result;
    }
}
