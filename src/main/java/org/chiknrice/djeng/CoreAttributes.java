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
package org.chiknrice.djeng;

import static org.chiknrice.djeng.XmlConfig.*;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public final class CoreAttributes {

    public static final Attribute ATTR_TYPE_MAPPER;
    public static final Attribute ID;
    public static final Attribute CODEC;
    public static final Attribute CLASS;
    public static final Attribute FILTER;
    public static final Attribute INDEX;
    public static final Attribute DESCRIPTION;
    public static final Attribute MASK; // TODO: implement mask in the schema
    public static final Attribute SUB_ELEMENT_CODECS_MAP;

    static {
        ATTR_TYPE_MAPPER = new Attribute("attr-type-mapper", NAMESPACE);
        ID = new Attribute("id", NAMESPACE);
        CODEC = new Attribute("codec", NAMESPACE);
        CLASS = new Attribute("class", NAMESPACE);
        FILTER = new Attribute("filter", NAMESPACE);
        INDEX = new Attribute("index", NAMESPACE);
        DESCRIPTION = new Attribute("description", NAMESPACE);
        MASK = new Attribute("packed", NAMESPACE);
        // doesn't map to actual xml attribute but used for composite codecs
        SUB_ELEMENT_CODECS_MAP = new Attribute("sub-element-codecs-map", NAMESPACE);
    }

    static Object mapType(Attribute attribute, String value, AttributeTypeMapper customTypeMapper) {
        try {
            Object objectValue = null;
            switch (attribute.getName()) {
                case "class":
                case "attr-type-mapper":
                    objectValue = Class.forName(value);
                    break;
                default:
            }
            if (objectValue == null && customTypeMapper != null) {
                objectValue = customTypeMapper.mapType(attribute, value);
            }
            if (objectValue == null) {
                return value;
            } else {
                return objectValue;
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
