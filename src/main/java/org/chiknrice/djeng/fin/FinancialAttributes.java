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
import org.chiknrice.djeng.AttributeTypeMapper;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public final class FinancialAttributes implements AttributeTypeMapper {

    public static final String NAMESPACE = "http://www.chiknrice.org/djeng/financial";

    public static final Attribute LENGTH;
    public static final Attribute LVAR_LENGTH;
    public static final Attribute LVAR_ENCODING;
    public static final Attribute FIXED_NUMERIC_ENCODING;
    public static final Attribute VAR_NUMERIC_ENCODING;
    public static final Attribute DATE_ENCODING;
    public static final Attribute BITMAP_ENCODING;
    public static final Attribute PATTERN;
    public static final Attribute TIMEZONE;
    public static final Attribute PADDING;
    public static final Attribute STRIP_PADDING;
    public static final Attribute LEFT_JUSTIFIED;
    public static final Attribute PACKED;

    static {
        LENGTH = new Attribute("length", NAMESPACE);
        LVAR_LENGTH = new Attribute("lvar-length", NAMESPACE);
        LVAR_ENCODING = new Attribute("lvar-encoding", NAMESPACE);
        BITMAP_ENCODING = new Attribute("bitmap-encoding", NAMESPACE);
        DATE_ENCODING = new Attribute("date-encoding", NAMESPACE);
        FIXED_NUMERIC_ENCODING = new Attribute("fixed-numeric-encoding", NAMESPACE);
        VAR_NUMERIC_ENCODING = new Attribute("var-numeric-encoding", NAMESPACE);
        PATTERN = new Attribute("pattern", NAMESPACE);
        TIMEZONE = new Attribute("timezone", NAMESPACE);
        PADDING = new Attribute("padding", NAMESPACE);
        STRIP_PADDING = new Attribute("strip-padding", NAMESPACE);
        LEFT_JUSTIFIED = new Attribute("left-justified", NAMESPACE);
        PACKED = new Attribute("packed", NAMESPACE);
    }

    @Override
    public Object mapType(Attribute attribute, String value) {
        switch (attribute.getName()) {
            case "length":
            case "lvar-length":
                return Integer.valueOf(value);
            case "bitmap-encoding":
                return Bitmap.Encoding.valueOf(value);
            case "date-encoding":
            case "lvar-encoding":
            case "fixed-numeric-encoding":
            case "var-numeric-encoding":
                return Encoding.valueOf(value);
            case "pattern":
            case "timezone":
            case "padding":
                return value;
            case "strip-padding":
            case "left-justified":
            case "packed":
                return Boolean.valueOf(value);
            default:
                return value;
        }
    }
}
