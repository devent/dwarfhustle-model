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

/**
 * Properties for {@link GameMapObject}.
 *
 * @author Erwin Müller, {@code <erwin@muellerpublic.de>}
 */
public interface GameMapObjectProperties {

    public static final int VISIBLE_POS = 0;

    public static final int FORBIDDEN_POS = 1;

    public static final int MODEL_POS = 2;

    public static final int TEX_POS = 3;

    public static final int SELECT_POS = 9;

    public static final int ELEVATED_POS = 10;

    void setP(PropertiesSet p);

    PropertiesSet getP();

    default void setHidden(boolean flag) {
        if (!flag) {
            getP().set(VISIBLE_POS);
        } else {
            getP().clear(VISIBLE_POS);
        }
    }

    default boolean isHidden() {
        return !getP().get(VISIBLE_POS);
    }

    default void setVisible(boolean flag) {
        if (flag) {
            getP().set(VISIBLE_POS);
        } else {
            getP().clear(VISIBLE_POS);
        }
    }

    default boolean isVisible() {
        return getP().get(VISIBLE_POS);
    }

    default void setForbidden(boolean flag) {
        if (flag) {
            getP().set(FORBIDDEN_POS);
        } else {
            getP().clear(FORBIDDEN_POS);
        }
    }

    default boolean isForbidden() {
        return getP().get(FORBIDDEN_POS);
    }

    default void setHaveModel(boolean flag) {
        if (flag) {
            getP().set(MODEL_POS);
        } else {
            getP().clear(MODEL_POS);
        }
    }

    default boolean isHaveModel() {
        return getP().get(MODEL_POS);
    }

    default void setHaveTex(boolean flag) {
        if (flag) {
            getP().set(TEX_POS);
        } else {
            getP().clear(TEX_POS);
        }
    }

    default boolean isHaveTex() {
        return getP().get(TEX_POS);
    }

    default void setCanSelect(boolean flag) {
        if (flag) {
            getP().set(SELECT_POS);
        } else {
            getP().clear(SELECT_POS);
        }
    }

    default boolean isCanSelect() {
        return getP().get(SELECT_POS);
    }

    default void setElevated(boolean flag) {
        if (flag) {
            getP().set(ELEVATED_POS);
        } else {
            getP().clear(ELEVATED_POS);
        }
    }

    default boolean isElevated() {
        return getP().get(ELEVATED_POS);
    }

}
