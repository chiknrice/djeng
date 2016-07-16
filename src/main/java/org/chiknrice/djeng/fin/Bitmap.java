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

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents a bitmap as defined by ISO8583/AS2805.  Provides methods to test if a bit is set or not and encapsulates
 * properties specific to different bitmap types like primary and secondary bitmap sizes and which bits are control
 * bit.
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
final class Bitmap {

    enum Encoding {
        BINARY(8, 8),
        HEX(16, 16),
        DATA_SET(2, 1);

        int primaryBitmapLength;
        int secondaryBitmapLength;

        Encoding(int primaryBitmapLength, int secondaryBitmapLength) {
            this.primaryBitmapLength = primaryBitmapLength;
            this.secondaryBitmapLength = secondaryBitmapLength;
        }
    }

    final byte[] bytes;
    final Encoding encoding;

    Bitmap(Encoding encoding) {
        this(new byte[16], encoding);
    }

    Bitmap(byte[] bytes, Encoding encoding) {
        this.bytes = bytes;
        this.encoding = encoding;
    }

    boolean isSet(int bit) {
        int byteIndex = byteIndex(bit);
        return byteIndex < bytes.length && (bytes[byteIndex] & mask(bit)) > 0;
    }

    boolean isControlBit(int bit) {
        boolean controlBit = false;
        switch (encoding) {
            case HEX:
            case BINARY:
                if (bit == 1) {
                    controlBit = true;
                }
                break;
            case DATA_SET:
                if (bit == 1 || bit == 17 || bit == 25) {
                    controlBit = true;
                }
                break;
            default:
        }
        return controlBit;
    }

    void set(int bit) {
        bytes[byteIndex(bit)] = (byte) (bytes[byteIndex(bit)] | mask(bit));
    }

    void unSet(int bit) {
        bytes[byteIndex(bit)] = (byte) (bytes[byteIndex(bit)] & (mask(bit) ^ 0xFF));
    }

    static int mask(int bit) {
        return 128 >> ((bit - 1) % 8);
    }

    static int byteIndex(int bit) {
        return (bit - 1) / 8;
    }

    @Override
    public String toString() {
        Set<Integer> setBits = new TreeSet<>();
        int bits = bytes.length * 8;
        for (int i = 1; i <= bits; i++) {
            if (isSet(i) && !isControlBit(i)) {
                setBits.add(i);
            }
        }
        return setBits.toString();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes) ^ encoding.hashCode();
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
            return Arrays.equals(bytes, other.bytes) && encoding.equals(other.encoding);
        }
    }

}
