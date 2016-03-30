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

import java.nio.ByteBuffer;

import static java.lang.String.format;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public final class ByteUtil {

    private static final String HEX = "0123456789ABCDEF";

    /**
     * Gets the hex value of a given character.  This operation is case insensitive.  This should be much more efficient
     * than Integer.parseInt(s, 16).
     *
     * @param c the hex character to be
     * @return
     * @throws IllegalArgumentException if the character is not a valid hex value (0 to F)
     */
    public static int hexValue(char c) {
        int value = HEX.indexOf(Character.toUpperCase(c));
        if (value == -1) {
            throw new IllegalArgumentException(String.format("Invalid hex char %s", c));
        }
        return value;
    }

    /**
     * Transforms bytes to an array of hex characters representing the nibbles
     *
     * @param bytes
     * @return
     */
    static char[] bytesToHexChars(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        int charPos = chars.length - 1;// LSB

        for (int bytePos = bytes.length - 1; bytePos >= 0; bytePos--) {
            chars[charPos--] = HEX.charAt(bytes[bytePos] & 0x0f);
            chars[charPos--] = HEX.charAt((bytes[bytePos] & 0xf0) >> 4);
        }
        return chars;
    }

    /**
     * Transforms an even number of hex characters to a byte[]
     *
     * @param chars
     * @return
     */
    static byte[] hexCharsToBytes(char[] chars) {
        if (chars.length % 2 > 0) {
            throw new IllegalArgumentException("Odd character hex chars");
        }
        byte[] bytes = new byte[chars.length / 2];

        int length = chars.length;

        for (int charPos = length - 1; charPos >= 0; charPos--) {
            int bytePos = bytes.length - ((length - charPos - 1) / 2) - 1;
            boolean hi = (length - charPos) % 2 == 0;

            char c = chars[charPos];
            int hex = hexValue(c);

            bytes[bytePos] |= (hex << (hi ? 4 : 0));
        }
        return bytes;
    }

    /**
     * Encodes a byte[] to a string of hex characters representing the nibbles
     *
     * @param bytes
     * @return
     */
    public static String encodeHex(byte[] bytes) {
        return new String(bytesToHexChars(bytes));
    }

    /**
     * Decodes a string of (even) hex characters to nibbles in a byte[]
     *
     * @param hex
     * @return
     */
    public static byte[] decodeHex(String hex) {
        return hexCharsToBytes(hex.toCharArray());
    }

    /**
     * Encodes a string of numeric characters to BCD.  If the string is odd characters it will be padded with zero '0'
     * at the first nibble.
     *
     * @param value the string to be encoded
     * @return the encoded value
     * @throws IllegalArgumentException if the string contains non numeric characters
     */
    public static byte[] encodeBcd(String value) {
        validateBcd(value);
        int length = value.length();
        int padLength = length % 2;
        if (padLength == 0) {
            return decodeHex(value);
        } else {
            char[] chars = new char[length + padLength];
            value.getChars(0, length, chars, 1);
            chars[0] = '0';
            return hexCharsToBytes(chars);
        }
    }

    /**
     * Decodes bytes to a string of numeric characters representing the nibbles.
     *
     * @param bytes the bytes to be decoded
     * @return the decoded numeric string
     * @throws IllegalArgumentException if the bytes contains nibbles with value above 9 (A-F)
     */
    public static String decodeBcd(byte[] bytes) {
        String decoded = encodeHex(bytes);
        validateBcd(decoded);
        return decoded;
    }

    /**
     * Encodes a string of numeric characters to BCD_F which is left justified and 'F' padded. This method expects the
     * first numeric character to be non '0'.  If the characters are odd it would be padded with 'F' at the last
     * nibble.
     *
     * @param value the string to be encoded
     * @return the encoded value
     * @throws IllegalArgumentException if the string contains non numeric characters
     */
    public static byte[] encodeBcdF(String value) {
        validateBcdF(value);
        int length = value.length();
        int padLength = length % 2;
        if (padLength == 0) {
            return decodeHex(value);
        } else {
            char[] chars = new char[length + padLength];
            value.getChars(0, length, chars, 0);
            chars[length] = 'F';
            return hexCharsToBytes(chars);
        }
    }

    /**
     * Decodes bytes to a string of numeric characters representing the nibbles.  The last nibble can be 'F' indicating
     * an odd number of numeric characters.  If the last nibble is 'F' it would be dropped.
     *
     * @param bytes the bytes to be decoded
     * @return the decoded numeric string
     * @throws IllegalArgumentException if the last nibble contains any value of A to E, or if the rest of the nibbles
     *                                  has a non numeric values
     */
    public static String decodeBcdF(byte[] bytes) {
        char[] chars = bytesToHexChars(bytes);
        int length = chars[chars.length - 1] == 'F' ? chars.length - 1 : chars.length;
        String decoded = new String(chars, 0, length);
        validateBcdF(decoded);
        return decoded;
    }

    /**
     * Encodes a string with even number of characters.  The first character can either be '0' or '-' while the rest is
     * expected to be numeric.  The '-' specifies a negative number.  If the first character is 0 it would be replaced
     * with 'C' and if it is '-' it would be replaced with 'D'.
     *
     * @param value the string to be encoded
     * @return the encoded value
     * @throws IllegalArgumentException if the value is odd number of character; or the first char of value is neither
     *                                  '0' nor '-'; or if the rest contains non numeric characters
     */
    public static byte[] encodeCBcd(String value) {
        validateCBcd(value);
        boolean credit = value.charAt(0) != '-';
        char[] chars = value.toCharArray();
        chars[0] = credit ? 'C' : 'D';
        return hexCharsToBytes(chars);
    }

    /**
     * Decodes bytes to a string which represents a positive or a negative numeric value.  The first nibble is expected
     * to be any of '0', 'C', or 'D'.  'D' represents a negative value while '0' or 'C' represents non negative value.
     *
     * @param bytes the bytes to be decoded
     * @return the decoded numeric string
     * @throws IllegalArgumentException if the first nibble is anything but '0', 'C', or 'D' or the rest contains non
     *                                  numeric characters
     */
    public static String decodeCBcd(byte[] bytes) {
        char[] chars = bytesToHexChars(bytes);
        chars[0] = chars[0] == 'D' ? '-' : chars[0] == 'C' ? chars[0] = '0' : chars[0];
        String decoded = new String(chars);
        validateCBcd(decoded);
        return decoded;
    }

    /**
     * Encodes a string with even number of characters.  The first two character can either be '00' or '-0' while the
     * rest is expected to be numeric.  '-0' indicates a negative number.  If the first two characters are '00' it would
     * be replaced with '43' (which is ASCII character 'C') and if it is '-0' it would be replaced with '44' (which is
     * ASCII character 'D').
     *
     * @param value the string to be encoded
     * @return the encoded value
     * @throws IllegalArgumentException if the value is odd number of character; or if the string doesn't start with
     *                                  either "00" or "-0"; or if the rest contains non numeric characters
     */
    public static byte[] encodeCcBcd(String value) {
        validateCcBcd(value);
        boolean credit = !value.startsWith("-0");
        char[] chars = value.toCharArray();
        chars[0] = '4';
        chars[1] = credit ? '3' : '4';
        return hexCharsToBytes(chars);
    }

    /**
     * Decodes bytes to a string which represents a positive or a negative numeric value.  The first byte is expected to
     * be any of '0x00', '0x43', or '0x44'.  '0x44' represents a negative value while '0x00' or '0x43' represents non
     * negative value.
     *
     * @param bytes the bytes to be decoded
     * @return the decoded numeric string
     * @throws IllegalArgumentException if the first byte is anything but '0x00', '0x43', or '0x44' or the rest contains
     *                                  non numeric characters
     */
    public static String decodeCcBcd(byte[] bytes) {
        String prefix = new String(bytes, 0, 1);
        prefix = prefix.equals("D") ? "-0" : prefix.equals("C") ? "00" : null;
        char[] chars = bytesToHexChars(bytes);
        if (prefix != null) {
            chars[0] = prefix.charAt(0);
            chars[1] = prefix.charAt(1);
        }
        String decoded = new String(chars);
        validateCcBcd(decoded);
        return decoded;
    }

    private static void validateBcd(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (!Character.isDigit(string.charAt(i))) {
                throw new IllegalArgumentException("Invalid BCD: " + string);
            }
        }
    }

    private static void validateBcdF(String string) {
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (i == 0 ? c == '0' : !Character.isDigit(c)) {
                throw new IllegalArgumentException("Invalid BCD_F: " + string);
            }
        }
    }

    private static void validateCBcd(String string) {
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (i == 0 ? c != '0' && c != '-' : !Character.isDigit(c)) {
                throw new IllegalArgumentException("Invalid C_BCD: " + string);
            }
        }
    }

    private static void validateCcBcd(String string) {
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (i == 0 ? c != '0' && c != '-' : i == 1 ? c != '0' : !Character.isDigit(c)) {
                throw new IllegalArgumentException("Invalid CC_BCD: " + string);
            }
        }
    }

    /**
     * Creates a new ByteBuffer with capacity = length from the current position.  Creation of the buffer consumes
     * length bytes.
     *
     * @param origBuffer
     * @param length
     * @return
     */
    public static ByteBuffer consumeToBuffer(ByteBuffer origBuffer, int length) {
        int origBufferLimit = origBuffer.limit();
        // will be new position of origBuffer
        int newBufferLimit = origBuffer.position() + length;
        // set new buffer limit
        origBuffer.limit(newBufferLimit);
        // perform slice
        ByteBuffer newBuffer = origBuffer.slice();
        // consume length
        origBuffer.position(newBufferLimit);
        // restore orig limit
        origBuffer.limit(origBufferLimit);
        return newBuffer;
    }

    /**
     * Creates a new ByteBuffer from the current position back up to length bytes
     *
     * @param origBuffer
     * @param length
     * @return
     */
    static ByteBuffer recallToBuffer(ByteBuffer origBuffer, int length) {
        origBuffer.position(origBuffer.position() - length);
        return consumeToBuffer(origBuffer, length);
    }

    public static byte[] encodeBinary(Long value) {
        return encodeBinary(value, 8);
    }

    public static byte[] encodeBinary(Integer value) {
        return encodeBinary(value, 4);
    }

    public static byte[] encodeBinary(Number value, int maxBytes) {
        int size;
        if (value instanceof Long) {
            size = 8;
        } else {
            size = 4;
        }
        ByteBuffer buf = ByteBuffer.allocate(size);
        if (value instanceof Long) {
            buf.putLong((Long) value);
        } else {
            buf.putInt((Integer) value);
        }

        buf.flip();
        byte[] bytes = buf.array();
        if (maxBytes < bytes.length) {
            byte[] trimmed = new byte[maxBytes];
            System.arraycopy(bytes, bytes.length - maxBytes, trimmed, 0, trimmed.length);

            // verify no data trimmed
            buf = ByteBuffer.allocate(size);
            buf.put(bytes, 0, bytes.length - maxBytes);
            buf.clear();
            if (((size == 8) ? buf.getLong() : buf.getInt()) > 0) {
                throw new RuntimeException(format("%s trimmed on encoding to %d bytes", value.toString(), maxBytes));
            }

            // use trimmed
            bytes = trimmed;
        }
        return bytes;
    }

    public static Long decodeBinaryLong(byte[] bytes) {
        return wrap(bytes, 8).getLong();
    }

    public static Integer decodeBinaryInt(byte[] bytes) {
        return wrap(bytes, 4).getInt();
    }

    private static ByteBuffer wrap(byte[] bytes, int maxBytes) {
        ByteBuffer buf = ByteBuffer.allocate(maxBytes);
        if (bytes.length < maxBytes) {
            buf.position(maxBytes - bytes.length);
        }
        buf.put(bytes);
        buf.clear();
        return buf;
    }

    public static void main(String[] args) {
        int i = Integer.parseInt("F1", 16);
        System.out.println(i);
        System.out.println((byte) i);
        System.out.println(((byte) i) & 0xFF);
        ByteBuffer b = ByteBuffer.allocate(10);
        b.put((byte) i);
        b.rewind();
        System.out.println(b.get());
        b.rewind();
        System.out.println(b.get() & 0xFF);
        b.put(b);
    }

}
