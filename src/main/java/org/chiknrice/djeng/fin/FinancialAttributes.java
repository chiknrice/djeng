/*
 * Copyright (c) 2016 Ian Bondoc
 *
 * This file is part of Djeng
 *
 * Djeng is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or(at your option) any later version.
 *
 * Djeng is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
package org.chiknrice.djeng.fin;

import org.chiknrice.djeng.Attribute;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public enum FinancialAttributes implements Attribute {

    LENGTH("length"),
    LVAR_LENGTH("lvar-length"),
    LVAR_ENCODING("lvar-encoding"),
    BITMAP_ENCODING("bitmap-encoding"),
    DATE_ENCODING("date-encoding"),
    FIXED_NUMERIC_ENCODING("fixed-numeric-encoding"),
    VAR_NUMERIC_ENCODING("var-numeric-encoding"),
    PATTERN("pattern"),
    TIMEZONE("timezone"),
    PADDING("padding"),
    STRIP_PADDING("strip-padding"),
    LEFT_JUSTIFIED("left-justified"),
    PACKED("packed");

    private final String name;
    private final String nameSpace;

    FinancialAttributes(String name) {
        this.name = name;
        this.nameSpace = "http://www.chiknrice.org/djeng/financial";
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getNamespace() {
        return this.nameSpace;
    }

    @Override
    public Object applyType(String value) throws Exception {
        switch (this) {
            case LENGTH:
            case LVAR_LENGTH:
                return Integer.valueOf(value);
            case BITMAP_ENCODING:
                return Bitmap.Encoding.valueOf(value);
            case DATE_ENCODING:
            case LVAR_ENCODING:
            case FIXED_NUMERIC_ENCODING:
            case VAR_NUMERIC_ENCODING:
                return Encoding.valueOf(value);
            case PATTERN:
            case TIMEZONE:
            case PADDING:
                return value;
            case STRIP_PADDING:
            case LEFT_JUSTIFIED:
            case PACKED:
                return Boolean.valueOf(value);
            default:
                throw new RuntimeException("Unexpected attribute " + this);
        }
    }
}
