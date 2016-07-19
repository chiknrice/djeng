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

import org.chiknrice.djeng.*;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TODO
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class PlainCompositeCodec extends CompositeCodec {

    /**
     * TODO
     * @param buffer TODO
     * @param compositeMap TODO
     * @param subElementsCodecs TODO
     */
    @Override
    protected void encodeSubElements(ByteBuffer buffer, CompositeMap compositeMap, Map<String, Codec> subElementsCodecs) {
        Set<String> elementsLeft = new HashSet<>(compositeMap.keySet());
        for (Map.Entry<String, Codec> codecEntry : subElementsCodecs.entrySet()) {
            String index = codecEntry.getKey();
            Object subElement = compositeMap.get(index);
            if (subElement == null) {
                throw new CodecException("Missing required element", index);
            }
            encodeSubElement(index, codecEntry.getValue(), buffer, subElement);
            elementsLeft.remove(index);
        }
        if (elementsLeft.size() > 0) {
            throw new RuntimeException("Unexpected sub elements: " + elementsLeft);
        }

    }

    /**
     * TODO
     * @param buffer TODO
     * @param subElementsCodecs TODO
     * @return TODO
     */
    @Override
    protected CompositeMap decodeSubElements(ByteBuffer buffer, Map<String, Codec> subElementsCodecs) {
        CompositeMap compositeMap = new CompositeMap();
        for (Map.Entry<String, Codec> subElementCodec : subElementsCodecs.entrySet()) {
            String index = subElementCodec.getKey();
            Object subElement = decodeSubElement(index, subElementCodec.getValue(), buffer);
            compositeMap.put(index, subElement);
        }
        return compositeMap;
    }

}
