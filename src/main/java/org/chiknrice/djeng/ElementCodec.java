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

import org.chiknrice.djeng.fin.FinancialAttribute;

import java.nio.ByteBuffer;

/**
 * TODO: document this for elements (as opposed to composites)
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public abstract class ElementCodec<T> extends Codec<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    public final void encode(ByteBuffer buffer, T element) {
        int pos = buffer.arrayOffset() + buffer.position();
        byte[] bytes = encodeValue(element);
        putDataBytes(buffer, bytes);
        int len = buffer.arrayOffset() + buffer.position() - pos;
        if (len > 0) {
            recordSection(pos, len, element, ByteUtil.recallToBuffer(buffer, len));
        }
    }

    /**
     * Encodes {@code T} to bytes.
     *
     * @param value the actual value to be encoded
     * @return TODO
     */
    protected abstract byte[] encodeValue(T value);

    /**
     * Puts the data bytes to the buffer.
     *
     * @param buffer the ByteBuffer to which the bytes would be encoded
     * @param bytes  TODO
     */
    protected void putDataBytes(ByteBuffer buffer, byte[] bytes) {
        buffer.put(bytes);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public T decode(ByteBuffer buffer) {
        int pos = buffer.arrayOffset() + buffer.position();
        byte[] bytes = getDataBytes(buffer);
        T element = decodeValue(bytes);
        int len = buffer.arrayOffset() + buffer.position() - pos;
        if (len > 0) {
            recordSection(pos, len, element, ByteUtil.recallToBuffer(buffer, len));
        }
        return element;
    }

    /**
     * Decodes the bytes to {@code T}.
     *
     * @param bytes TODO
     * @return TODO
     */
    protected abstract T decodeValue(byte[] bytes);

    /**
     * Gets the data bytes from the buffer.
     *
     * @param buffer TODO
     * @return TODO
     */
    protected byte[] getDataBytes(ByteBuffer buffer) {
        Integer length = getAttribute(FinancialAttribute.LENGTH);
        byte[] bytes;
        if (length != null) {
            bytes = new byte[length];
        } else {
            bytes = new byte[buffer.remaining()];
        }
        buffer.get(bytes);
        return bytes;
    }

}
