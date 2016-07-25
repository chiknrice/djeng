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

import org.chiknrice.djeng.ByteUtil;
import org.chiknrice.djeng.ElementCodec;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class NumericCodec extends ElementCodec<Object> implements LengthPrefixDelegate {

    enum NumericType {
        INTEGER,
        LONG,
        BIG_INTEGER,
        STRING
    }

    @Override
    protected byte[] encodeValue(Object value) {
        Integer length = getAttribute(FinancialAttribute.LENGTH);
        if (length != null) {
            return encodeFixedLength(length, value);
        } else {
            return encodeVarLength(value);
        }
    }

    /**
     * Fixed length values are always padded and padding is always zero '0'.  If a value is negative the hyphen is part
     * of the length.
     *
     * @param length
     * @param value
     * @return
     */
    private byte[] encodeFixedLength(int length, Object value) {
        Encoding encoding = getAttribute(FinancialAttribute.FIXED_NUMERIC_ENCODING);
        if (Encoding.CC_BCD.equals(encoding)) {
            // additional 2 characters needs to be allotted for hex of C/D
            length += 2;
        }
        NumericType numericType = getAttribute(FinancialAttribute.NUMERIC_TYPE);
        String stringValue;
        switch (numericType) {
            case INTEGER:
                if (value instanceof Integer) {
                    stringValue = String.format("%0" + length + "d", ((Integer) value).intValue());
                    break;
                }
            case LONG:
                if (value instanceof Long) {
                    stringValue = String.format("%0" + length + "d", ((Long) value).longValue());
                    break;
                }
            case BIG_INTEGER:
                if (value instanceof BigInteger) {
                    //BigInteger bigInteger = (BigInteger) value;
                    // TODO implement this
                    throw new UnsupportedOperationException("BigInteger not yet supported");
                    //break;
                }
            case STRING:
                stringValue = value.toString();
                break;
            default:
                throw new RuntimeException("Unexpected numeric type " + value.getClass().getName());
        }

        byte[] bytes;
        switch (encoding) {
            case CHAR:
                bytes = stringValue.getBytes(StandardCharsets.ISO_8859_1);
                break;
            case BCD:
                bytes = ByteUtil.encodeBcd(stringValue);
                break;
            case C_BCD:
                bytes = ByteUtil.encodeCBcd(stringValue);
                break;
            case CC_BCD:
                bytes = ByteUtil.encodeCcBcd(stringValue);
                break;
            default:
                throw new RuntimeException("Unsupported fixed length numeric encoding " + encoding);
        }
        return bytes;
    }

    private byte[] encodeVarLength(Object value) {
        Encoding encoding = getAttribute(FinancialAttribute.VAR_NUMERIC_ENCODING);
        byte[] bytes;
        String stringValue = value.toString();
        switch (encoding) {
            case CHAR:
                bytes = stringValue.getBytes(StandardCharsets.ISO_8859_1);
                break;
            case BCD:
                bytes = ByteUtil.encodeBcd(stringValue);
                break;
            case BCD_F:
                bytes = ByteUtil.encodeBcdF(stringValue);
                break;
            default:
                throw new RuntimeException("Unsupported var length numeric encoding " + encoding);
        }
        return bytes;
    }

    @Override
    protected Object decodeValue(byte[] bytes) {
        Encoding encoding = getAttribute(FinancialAttribute.FIXED_NUMERIC_ENCODING);
        String stringValue;
        if (encoding != null) {
            stringValue = decodeFixedLength(encoding, bytes);
        } else {
            stringValue = decodeVarLength(bytes);
        }
        Boolean stripPadding = getAttribute(FinancialAttribute.STRIP_PADDING);
        if (stripPadding != null && stripPadding) {
            // TODO probably not needed here as doing Integer.valueOf("-000001") results in -1
            throw new UnsupportedOperationException("Strip padding not yet supported");
        }
        NumericType numericType = getAttribute(FinancialAttribute.NUMERIC_TYPE);
        switch (numericType) {
            case INTEGER:
                return Integer.valueOf(stringValue);
            case LONG:
                return Long.valueOf(stringValue);
            case STRING:
                return stringValue;
            case BIG_INTEGER:
            default:
                throw new UnsupportedOperationException(numericType + " not yet supported");
        }
    }

    private String decodeFixedLength(Encoding encoding, byte[] bytes) {
        String stringValue;
        switch (encoding) {
            case CHAR:
                stringValue = new String(bytes, StandardCharsets.ISO_8859_1);
                break;
            case BCD:
                stringValue = ByteUtil.decodeBcd(bytes);
                break;
            case C_BCD:
                stringValue = ByteUtil.decodeCBcd(bytes);
                break;
            case CC_BCD:
                stringValue = ByteUtil.decodeCcBcd(bytes);
                break;
            default:
                throw new RuntimeException("Unsupported fixed length numeric encoding " + encoding);
        }
        return stringValue;
    }

    private String decodeVarLength(byte[] bytes) {
        Encoding encoding = getAttribute(FinancialAttribute.VAR_NUMERIC_ENCODING);
        String stringValue;
        switch (encoding) {
            case CHAR:
                stringValue = new String(bytes, StandardCharsets.ISO_8859_1);
                break;
            case BCD:
                stringValue = ByteUtil.decodeBcd(bytes);
                break;
            case BCD_F:
                stringValue = ByteUtil.decodeBcdF(bytes);
                break;
            default:
                throw new RuntimeException("Unsupported var length numeric encoding " + encoding);
        }
        return stringValue;
    }


    @Override
    protected byte[] getDataBytes(ByteBuffer buffer) {
        Integer length = getAttribute(FinancialAttribute.LENGTH);
        byte[] bytes;
        if (length != null) {
            Encoding encoding = getAttribute(FinancialAttribute.FIXED_NUMERIC_ENCODING);
            int dataBytesCount;
            switch (encoding) {
                case CHAR:
                    dataBytesCount = length;
                    break;
                case BCD:
                case C_BCD:
                    dataBytesCount = length / 2 + length % 2;
                    break;
                case CC_BCD:
                    dataBytesCount = (length / 2 + length % 2) + 1;
                    break;
                default:
                    throw new RuntimeException("Unsupported fixed length numeric encoding " + encoding);
            }
            bytes = new byte[dataBytesCount];
        } else {
            bytes = new byte[buffer.remaining()];
        }
        buffer.get(bytes);
        return bytes;
    }

    @Override
    public int determineLengthPrefixValue(Object value) {
        return value.toString().length();
    }

    @Override
    public int determineDataBytesCount(int dataLength) {
        int dataByteCount;
        Encoding encoding = getAttribute(FinancialAttribute.VAR_NUMERIC_ENCODING);
        switch (encoding) {
            case CHAR:
                dataByteCount = dataLength;
                break;
            case BCD:
            case BCD_F:
                dataByteCount = dataLength / 2 + dataLength % 2;
                break;
            default:
                throw new RuntimeException("Unsupported var length numeric encoding " + encoding);
        }
        return dataByteCount;
    }
}
