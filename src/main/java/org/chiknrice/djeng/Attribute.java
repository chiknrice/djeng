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

/**
 *
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class Attribute {

    private final String name;
    private final String nameSpace;
    private final String absoluteName;

    public Attribute(String name, String nameSpace) {
        this.name = name;
        this.nameSpace = nameSpace;
        this.absoluteName = String.format("{%s}%s", nameSpace, name);
    }

    public String getName() {
        return this.name;
    }

    public String getNamespace() {
        return this.nameSpace;
    }

    @Override
    public int hashCode() {
        return absoluteName.hashCode();
    }

    @Override
    public String toString() {
        return absoluteName;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == null ? false : obj instanceof Attribute ? absoluteName.equals(((Attribute) obj).absoluteName) : false;
    }
}
