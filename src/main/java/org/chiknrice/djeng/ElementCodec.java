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
public abstract class ElementCodec<T> extends BaseCodec<T> {

    /**
     * @param buffer  the ByteBuffer where the element would be encoded
     * @param element the element to be encoded
     */
    public final void encode(ByteBuffer buffer, MessageElement<T> element) {
        T value = element.getValue();
        byte[] rawValue = encodeValue(value);
        encodeRawValue(buffer, rawValue);
    }

    /**
     * Encode the value to raw value.
     *
     * @param value the actual value to be encoded
     * @return TODO
     */
    protected abstract byte[] encodeValue(T value);

    /**
     * Encode the raw value to the buffer.
     *
     * @param buffer   the ByteBuffer to which the value would be encoded
     * @param value TODO
     */
    protected void encodeRawValue(ByteBuffer buffer, byte[] value) {
        buffer.put(value);
    }


    /**
     * @param buffer TODO
     * @return TODO
     */
    @Override
    public MessageElement<T> decode(ByteBuffer buffer) {
        byte[] rawValue = decodeRawValue(buffer);
        T value = decodeValue(rawValue);
        return new MessageElement<>(value);
    }

    /**
     * Decode the raw value to the value.
     *
     * @param rawValue TODO
     * @return TODO
     */
    protected abstract T decodeValue(byte[] rawValue);

    /**
     * Decode the raw value from the buffer.
     *
     * @param buffer TODO
     * @return TODO
     */
    protected byte[] decodeRawValue(ByteBuffer buffer) {
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
