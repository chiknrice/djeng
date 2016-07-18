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

import static org.chiknrice.djeng.XmlConfig.NAMESPACE;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public enum CoreAttributes implements Attribute {
    ID("id", NAMESPACE),
    CODEC("codec", NAMESPACE),
    CLASS("class", NAMESPACE),
    INDEX("index", NAMESPACE),
    DESCRIPTION("description", NAMESPACE),
    MASK("packed", NAMESPACE),
    // doesn't map to actual xml attribute but used for composite codecs
    SUB_ELEMENT_CODECS_MAP("sub-element-codecs-map", NAMESPACE);

    private final String name;
    private final String nameSpace;

    CoreAttributes(String name, String nameSpace) {
        this.name = name;
        this.nameSpace = nameSpace;
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
        if ("class".equals(name)) {
            return Class.forName(value.toString());
        }
        return value;
    }

}
