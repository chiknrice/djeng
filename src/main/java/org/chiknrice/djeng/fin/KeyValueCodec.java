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

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * The {@code KeyValueCodec} is a {@code CompositeCodec} which encodes/decodes multiple key-value sub-elements.  During
 * encoding, the index would be treated as the key and the message element as the value and during decoding the key
 * would serve as the index of the message element.  The ordering of the elements is not guarantied when encoding.
 * <p/>
 * The configuration expects a sub-element {@code Codec<String>} with index="key" to be used for encoding/decoding the
 * key and a sub-element codec with index="value" for encoding/decoding the values.
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class KeyValueCodec extends CompositeCodec {

    /**
     * @param buffer            TODO
     * @param compositeMap      TODO
     * @param subElementsCodecs TODO
     */
    @Override
    protected void encodeSubElements(ByteBuffer buffer, CompositeMap compositeMap, Map<String, Codec> subElementsCodecs) {
        Codec keyCodec = subElementsCodecs.get("key");
        Codec valueCodec = subElementsCodecs.get("value");
        if (subElementsCodecs.size() != 2 && keyCodec == null || valueCodec == null) {
            throw new RuntimeException("Invalid " + KeyValueCodec.class.getSimpleName() + " configuration");
        }
        for (Map.Entry<String, Object> entry : compositeMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            try {
                pushIndex(getKeyIndex());
                keyCodec.encode(buffer, key);
            } finally {
                popIndex();
            }
            try {
                pushIndex(key);
                valueCodec.encode(buffer, value);
            } finally {
                popIndex();
            }
        }
    }

    protected String getKeyIndex() {
        return "key";
    }

    /**
     * Decodes the sub-elements using the key and value codecs which are specified by the attributes {@code key} and
     * {@code value}.  Decoding would consume the {@code ByteBuffer} up to the limit.
     *
     * @param buffer            TODO
     * @param subElementsCodecs TODO
     * @return TODO
     */
    @Override
    protected CompositeMap decodeSubElements(ByteBuffer buffer, Map<String, Codec> subElementsCodecs) {
        Codec keyCodec = subElementsCodecs.get("key");
        Codec valueCodec = subElementsCodecs.get("value");
        if (subElementsCodecs.size() != 2 && keyCodec == null || valueCodec == null) {
            throw new RuntimeException("Invalid " + KeyValueCodec.class.getSimpleName() + " configuration");
        }
        CompositeMap compositeMap = new CompositeMap();
        String key;
        while (buffer.hasRemaining()) {
            try {
                pushIndex(getKeyIndex());
                key = (String) keyCodec.decode(buffer);
            } finally {
                popIndex();
            }
            Object value;
            try {
                pushIndex(key);
                value = valueCodec.decode(buffer);
            } finally {
                popIndex();
            }
            compositeMap.put(key, value);
        }

        return compositeMap;
    }

}
