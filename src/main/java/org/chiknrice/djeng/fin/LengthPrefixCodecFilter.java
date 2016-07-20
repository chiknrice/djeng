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
import org.chiknrice.djeng.Codec;
import org.chiknrice.djeng.CodecFilter;
import org.chiknrice.djeng.ElementCodec;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.chiknrice.djeng.fin.FinancialAttribute.LVAR_ENCODING;
import static org.chiknrice.djeng.fin.FinancialAttribute.LVAR_LENGTH;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public final class LengthPrefixCodecFilter<T> extends CodecFilter<T, T> {

    private final LengthPrefixCodec lengthPrefixCodec;

    public LengthPrefixCodecFilter() {
        lengthPrefixCodec = new LengthPrefixCodec();
    }

    @Override
    public void encode(ByteBuffer buffer, T element, Codec<T> chain) {
        int lengthPrefixBytesCount = lengthPrefixCodec.getLengthPrefixBytesCount();
        buffer.mark();
        buffer.position(buffer.position() + lengthPrefixBytesCount);
        ByteBuffer dataBuffer = buffer.slice();
        chain.encode(dataBuffer, element);
        buffer.reset();
        int pos = buffer.arrayOffset() + buffer.position();
        int valueLength = dataBuffer.position();
        LengthPrefixDelegate delegate = getDelegate(LengthPrefixDelegate.class);
        if (delegate != null) {
            valueLength = delegate.determineLengthPrefixValue(element);
        }
        try {
            pushIndex("len");
            lengthPrefixCodec.encode(buffer, valueLength);
        } finally {
            popIndex();
        }
        buffer.position(buffer.position() + dataBuffer.position());
    }

    @Override
    public T decode(ByteBuffer buffer, Codec<T> chain) {
        int dataLength;
        try {
            pushIndex("len");
            dataLength = lengthPrefixCodec.decode(buffer);
        } finally {
            popIndex();
        }
        int dataByteCount = dataLength;
        LengthPrefixDelegate delegate = getDelegate(LengthPrefixDelegate.class);
        if (delegate != null) {
            dataByteCount = delegate.determineDataBytesCount(dataLength);
        }
        if (dataByteCount > buffer.remaining()) {
            throw new RuntimeException(String.format("Not enough bytes in buffer for var length %d", dataByteCount));
        }
        ByteBuffer dataBuffer = ByteUtil.consumeToBuffer(buffer, dataByteCount);
        T element = chain.decode(dataBuffer);
        return element;
    }

    private class LengthPrefixCodec extends ElementCodec<Integer> {

        @Override
        protected byte[] encodeValue(Integer value) {
            Integer lengthDigits = LengthPrefixCodecFilter.this.getAttribute(LVAR_LENGTH);
            Encoding encoding = LengthPrefixCodecFilter.this.getAttribute(LVAR_ENCODING);
            String numericString = String.format("%0" + lengthDigits + "d", value);
            byte[] bytes;
            switch (encoding) {
                case BCD:
                    bytes = ByteUtil.encodeBcd(numericString);
                    break;
                case CHAR:
                    bytes = numericString.getBytes(StandardCharsets.ISO_8859_1);
                    break;
                case BINARY:
                    bytes = ByteUtil.encodeBinary(value, lengthDigits);
                    break;
                default:
                    throw new RuntimeException(String.format("Unsupported length prefix encoding: %s", encoding));
            }
            return bytes;
        }

        @Override
        protected Integer decodeValue(byte[] rawValue) {
            Encoding encoding = LengthPrefixCodecFilter.this.getAttribute(LVAR_ENCODING);
            int dataLength;
            switch (encoding) {
                case BCD:
                    dataLength = Integer.parseInt(ByteUtil.decodeBcd(rawValue));
                    break;
                case CHAR:
                    dataLength = Integer.parseInt(new String(rawValue, StandardCharsets.ISO_8859_1));
                    break;
                case BINARY:
                    dataLength = ByteUtil.decodeBinaryInt(rawValue);
                    break;
                default:
                    throw new RuntimeException(String.format("Unsupported length prefix encoding %s", encoding));
            }
            return dataLength;
        }

        @Override
        protected byte[] decodeRawValue(ByteBuffer buffer) {
            byte[] lengthPrefixBytes = new byte[getLengthPrefixBytesCount()];
            buffer.get(lengthPrefixBytes);
            return lengthPrefixBytes;
        }

        private int getLengthPrefixBytesCount() {
            int lengthPrefixBytes;
            Integer lengthDigits = LengthPrefixCodecFilter.this.getAttribute(LVAR_LENGTH);
            Encoding encoding = LengthPrefixCodecFilter.this.getAttribute(LVAR_ENCODING);
            switch (encoding) {
                case BCD:
                    lengthPrefixBytes = lengthDigits / 2 + lengthDigits % 2;
                    break;
                case CHAR:
                case BINARY:
                    lengthPrefixBytes = lengthDigits;
                    break;
                default:
                    throw new RuntimeException(String.format("Unsupported length prefix encoding: %s", encoding));
            }
            return lengthPrefixBytes;
        }
    }

}
