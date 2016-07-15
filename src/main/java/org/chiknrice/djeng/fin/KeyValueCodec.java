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

import org.chiknrice.djeng.Codec;
import org.chiknrice.djeng.CompositeCodec;
import org.chiknrice.djeng.CompositeMap;
import org.chiknrice.djeng.MessageElement;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * The {@code KeyValueCodec} is a {@code CompositeCodec} which maps the sub elements to keys and values.  The index of
 * the {@code MessageElement} being the key and the {@code MessageElement} value as the value.
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public abstract class KeyValueCodec<K> extends CompositeCodec {

    /**
     * @param buffer TODO
     * @param compositeMap TODO
     * @param subElementsCodecs TODO
     */
    @Override
    protected void encodeSubElements(ByteBuffer buffer, CompositeMap compositeMap, Map<String, Codec<?>> subElementsCodecs) {
        Codec keyCodec = subElementsCodecs.get("key");
        Codec valueCodec = subElementsCodecs.get("value");
        if (subElementsCodecs.size() != 2 && keyCodec == null || valueCodec == null) {
            throw new RuntimeException("Invalid " + KeyValueCodec.class.getSimpleName() + " configuration");
        }
        for (Map.Entry<String, MessageElement<?>> entry : compositeMap.entrySet()) {
            Object keyValue = toKeyValue(entry.getKey());
            MessageElement<?> valueElement = entry.getValue();
            MessageElement<Object> keyElement = new MessageElement<>(keyValue);
            keyCodec.encode(buffer, keyElement);
            valueElement.copySections(keyElement);
            valueCodec.encode(buffer, valueElement);
        }
    }

    /**
     * TODO
     * @param stringKey TODO
     * @return TODO
     */
    protected abstract K toKeyValue(String stringKey);

    /**
     * Decodes the sub-elements using the key and value codecs which are specified by the attributes {@code key} and
     * {@code value}.  Decoding would consume the {@code ByteBuffer} up to the limit.
     *
     * @param buffer TODO
     * @param subElementsCodecs TODO
     * @return TODO
     */
    @Override
    protected CompositeMap decodeSubElements(ByteBuffer buffer, Map<String, Codec<?>> subElementsCodecs) {
        Codec keyCodec = subElementsCodecs.get("key");
        Codec valueCodec = subElementsCodecs.get("value");
        if (subElementsCodecs.size() != 2 && keyCodec == null || valueCodec == null) {
            throw new RuntimeException("Invalid " + KeyValueCodec.class.getSimpleName() + " configuration");
        }
        CompositeMap compositeMap = new CompositeMap();
        while (buffer.hasRemaining()) {
            MessageElement keyElement = keyCodec.decode(buffer);
            MessageElement valueElement = valueCodec.decode(buffer);
            valueElement.copySections(keyElement);
            compositeMap.put(toKeyString((K) keyElement.getValue()), valueElement);
        }

        return compositeMap;
    }

    /**
     * TODO
     * @param key TODO
     * @return TODO
     */
    protected abstract String toKeyString(K key);

}
