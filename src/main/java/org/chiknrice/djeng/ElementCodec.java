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

/**
 * TODO: document this for elements (as opposed to composites)
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public abstract class ElementCodec<V, R> extends BaseCodec<V> {

    /**
     * @param buffer the ByteBuffer where the element would be encoded
     * @param element the element to be encoded
     */
    public final void encode(ByteBuffer buffer, MessageElement<V> element) {
        int pos = buffer.arrayOffset() + buffer.position();
        V value = element.getValue();
        R rawValue = encodeValue(value);
        encodeRawValue(buffer, rawValue);
        element.addSection(super.<String>getAttribute(CoreAttributes.INDEX), pos, buffer.arrayOffset() + buffer.position());
    }

    /**
     * Encode the value to raw value.
     *
     * @param value the actual value to be encoded
     * @return TODO
     */
    protected abstract R encodeValue(V value);

    /**
     * Encode the raw value to the buffer.
     *
     * @param buffer the ByteBuffer to which the value would be encoded
     * @param rawValue TODO
     */
    protected abstract void encodeRawValue(ByteBuffer buffer, R rawValue);


    /**
     * @param buffer TODO
     * @return TODO
     */
    @Override
    public MessageElement<V> decode(ByteBuffer buffer) {
        int pos = buffer.arrayOffset() + buffer.position();
        R rawValue = decodeRawValue(buffer);
        V value = decodeValue(rawValue);
        MessageElement<V> element = new MessageElement<>(value);
        element.addSection(super.<String>getAttribute(CoreAttributes.INDEX), pos, buffer.arrayOffset() + buffer.position());
        return element;
    }

    /**
     * Decode the raw value to the value.
     *
     * @param rawValue TODO
     * @return TODO
     */
    protected abstract V decodeValue(R rawValue);

    /**
     * Decode the raw value from the buffer.
     *
     * @param buffer TODO
     * @return TODO
     */
    protected abstract R decodeRawValue(ByteBuffer buffer);

}
