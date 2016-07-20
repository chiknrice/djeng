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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class NumericCodec extends ElementCodec<Long> implements LengthPrefixDelegate {

    @Override
    protected byte[] encodeValue(Long value) {
        // TODO: implement the padding here
        String stringValue;
        Encoding encoding;
        Integer length = getAttribute(FinancialAttribute.LENGTH);
        byte[] bytes;
        if (length != null) {
            encoding = getAttribute(FinancialAttribute.FIXED_NUMERIC_ENCODING);
            switch (encoding) {
                case CHAR:
                    // length is the same
                    break;
                case BCD:
                case C_BCD:
                    length += length % 2;
                    break;
                case CC_BCD:
                    length += 2;
                    break;
                default:
                    throw new RuntimeException("Unsupported fixed length numeric encoding " + encoding);
            }
            stringValue = String.format("%0" + length + "d", value);
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
        } else {
            stringValue = value.toString();
            encoding = getAttribute(FinancialAttribute.VAR_NUMERIC_ENCODING);
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

        }
        return bytes;
    }

    @Override
    protected Long decodeValue(byte[] bytes) {
        Encoding encoding = getAttribute(FinancialAttribute.FIXED_NUMERIC_ENCODING);
        String stringValue;
        if (encoding != null) {
            switch (encoding) {
                case CHAR:
                    // for now we make sure to trim all spaces if spaces are the padding
                    stringValue = new String(bytes, StandardCharsets.ISO_8859_1).trim();
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
        } else {
            encoding = getAttribute(FinancialAttribute.VAR_NUMERIC_ENCODING);
            switch (encoding) {
                case CHAR:
                    // for now we make sure to trim all spaces if spaces are the padding
                    stringValue = new String(bytes, StandardCharsets.ISO_8859_1).trim();
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
        }
        return Long.valueOf(stringValue.trim());
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
