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
import java.util.HashSet;
import java.util.Set;

/**
 * The {@code ArrayCodec} defines the encoding and decoding of array elements.  It is implemented as a filter which just
 * delegates encoding and decoding of the elements to the filtered codec.  Elements can then be any type of value (even
 * composite).
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class ArrayCodec extends CodecFilter<CompositeMap> {

    /**
     * TODO
     *
     * @param buffer  TODO
     * @param element TODO
     * @param chain   TODO
     */
    @Override
    protected void encode(ByteBuffer buffer, MessageElement<CompositeMap> element, Codec chain) {
        int pos = buffer.arrayOffset() + buffer.position();
        CompositeMap compositeMap = element.getValue();
        Set<String> elementsLeft = new HashSet<>(compositeMap.keySet());
        int index = 0;
        MessageElement<?> arrayElement;
        while ((arrayElement = compositeMap.get(Integer.toString(index))) != null) {
            chain.encode(buffer, arrayElement);
            elementsLeft.remove(Integer.toString(index++));
        }
        if (elementsLeft.size() > 0) {
            throw new RuntimeException("Unexpected array elements: " + elementsLeft);
        }
        element.addSection(pos, buffer.arrayOffset() + buffer.position(), compositeMap);
    }

    /**
     * TODO
     *
     * @param buffer TODO
     * @param chain  TODO
     * @return TODO
     */
    @Override
    protected MessageElement<CompositeMap> decode(ByteBuffer buffer, Codec chain) {
        int pos = buffer.arrayOffset() + buffer.position();
        CompositeMap compositeMap = new CompositeMap();
        int index = 0;
        while (buffer.hasRemaining()) {
            compositeMap.put(Integer.toString(index++), chain.decode(buffer));
        }
        MessageElement<CompositeMap> element = new MessageElement<>(compositeMap);
        element.addSection(pos, buffer.arrayOffset() + buffer.position(), compositeMap);
        return element;
    }
}
