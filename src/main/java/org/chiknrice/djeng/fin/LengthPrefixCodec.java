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
import org.chiknrice.djeng.MessageElement;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.chiknrice.djeng.fin.FinancialAttributes.LVAR_ENCODING;
import static org.chiknrice.djeng.fin.FinancialAttributes.LVAR_LENGTH;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public final class LengthPrefixCodec<T> extends CodecFilter<T, T> {

    @Override
    public void encode(ByteBuffer buffer, MessageElement<T> element, Codec<T> chain) {
        int lengthPrefixBytesCount = getLengthPrefixBytesCount();
        buffer.mark();
        buffer.position(buffer.position() + lengthPrefixBytesCount);
        ByteBuffer dataBuffer = buffer.slice();
        chain.encode(dataBuffer, element);
        buffer.reset();
        int pos = buffer.arrayOffset() + buffer.position();
        int limit = pos + lengthPrefixBytesCount;
        int valueLength = dataBuffer.position();
        LengthPrefixDelegate delegate = getDelegate(chain, LengthPrefixDelegate.class);
        if (delegate != null) {
            valueLength = delegate.determineLengthPrefixValue(element.getValue());
        }
        encodeLengthPrefix(buffer, valueLength);
        element.addSection(pos, limit, valueLength);
        buffer.position(buffer.position() + dataBuffer.position());
    }

    /**
     * Encodes the length prefix of a var length data element
     *
     * @param buffer
     * @param dataLength
     */
    private void encodeLengthPrefix(ByteBuffer buffer, int dataLength) {
        Integer lengthDigits = getAttribute(LVAR_LENGTH);
        Encoding encoding = getAttribute(LVAR_ENCODING);
        String numericString = String.format("%0" + lengthDigits + "d", dataLength);
        switch (encoding) {
            case BCD:
                buffer.put(ByteUtil.encodeBcd(numericString));
                break;
            case CHAR:
                buffer.put(numericString.getBytes(StandardCharsets.ISO_8859_1));
                break;
            case BINARY:
                buffer.put(ByteUtil.encodeBinary(dataLength, lengthDigits));
                break;
            default:
                throw new RuntimeException(String.format("Unsupported length prefix encoding: %s", encoding));
        }
    }

    @Override
    public MessageElement<T> decode(ByteBuffer buffer, Codec chain) {
        int pos = buffer.arrayOffset() + buffer.position();
        int dataLength = decodeLengthPrefix(buffer);
        int limit = buffer.arrayOffset() + buffer.position();
        int dataByteCount = dataLength;
        LengthPrefixDelegate delegate = getDelegate(chain, LengthPrefixDelegate.class);
        if (delegate != null) {
            dataByteCount = delegate.determineDataBytesCount(dataLength);
        }
        if (dataByteCount > buffer.remaining()) {
            throw new RuntimeException(String.format("Not enough bytes in buffer for var length %d", dataByteCount));
        }
        ByteBuffer dataBuffer = ByteUtil.consumeToBuffer(buffer, dataByteCount);
        MessageElement<T> element = chain.decode(dataBuffer);
        element.addSection(pos, limit, dataLength);
        return element;
    }

    /**
     * Decodes the length prefix of a var length data element
     *
     * @param buffer
     * @return the length prefix value
     */
    private int decodeLengthPrefix(ByteBuffer buffer) {
        Encoding encoding = getAttribute(LVAR_ENCODING);
        byte[] lengthPrefixBytes = new byte[getLengthPrefixBytesCount()];
        buffer.get(lengthPrefixBytes);
        int dataLength;
        switch (encoding) {
            case BCD:
                dataLength = Integer.parseInt(ByteUtil.decodeBcd(lengthPrefixBytes));
                break;
            case CHAR:
                dataLength = Integer.parseInt(new String(lengthPrefixBytes, StandardCharsets.ISO_8859_1));
                break;
            case BINARY:
                dataLength = ByteUtil.decodeBinaryInt(lengthPrefixBytes);
                break;
            default:
                throw new RuntimeException(String.format("Unsupported length prefix encoding %s", encoding));
        }
        return dataLength;
    }

    private int getLengthPrefixBytesCount() {
        int lengthPrefixBytes;
        Integer lengthDigits = getAttribute(LVAR_LENGTH);
        Encoding encoding = getAttribute(LVAR_ENCODING);
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
