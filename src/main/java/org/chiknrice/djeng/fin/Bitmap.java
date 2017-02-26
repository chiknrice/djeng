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

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * The {@code Bitmap} class represents a bitmap as defined by ISO8583/AS2805.  It provides methods to set and test a bit
 * and encapsulates primary and secondary bitmap sizes of different bitmap types.  It is also used to represent the
 * bitmap in a data set as specified in ISO8583 2003.  It is {@code Iterable} and will iterate through the bits in the
 * natural order of numbers.
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public final class Bitmap implements Iterable<Integer> {

    /**
     * The {@code Bitmap.Encoding} represents a of bitmap.  The {@code BINARY} encoding represents 8 bytes of bitmap as
     * 8 bytes while {@code HEX} represents a byte in a bitmap as 2 hex characters which would result in 2 bytes.  The
     * {@code DATA_SET} on the other hand represents a bitmap used in data sets for ISO8583 2003.
     */
    public enum Encoding {
        BINARY(8, 8),
        HEX(8, 8),
        DATA_SET(2, 1);

        int primaryBitmapLength;
        int secondaryBitmapLength;

        Encoding(int primaryBitmapLength, int secondaryBitmapLength) {
            this.primaryBitmapLength = primaryBitmapLength;
            this.secondaryBitmapLength = secondaryBitmapLength;
        }
    }

    private final Set<Integer> setBits;

    public Bitmap() {
        setBits = new TreeSet<>();
    }

    public boolean isSet(int bit) {
        return setBits.contains(bit);
    }

    public void set(int bit) {
        setBits.add(bit);
    }

    @Override
    public Iterator<Integer> iterator() {
        return setBits.iterator();
    }

    @Override
    public String toString() {
        return setBits.toString();
    }

    @Override
    public int hashCode() {
        return setBits.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o.getClass() != getClass()) {
            return false;
        } else {
            Bitmap other = (Bitmap) o;
            return setBits.equals(other.setBits);
        }
    }

}
