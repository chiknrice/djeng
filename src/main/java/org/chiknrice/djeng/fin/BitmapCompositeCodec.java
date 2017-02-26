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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The {@code BitmapCompositeCodec} class is a {@code CompositeCodec} which encodes a bitmap to indicate which elements
 * were included in the encoded composite element.  This codec also expects and decodes a bitmap which indicates which
 * elements are present as part of the composite element.
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public final class BitmapCompositeCodec extends CompositeCodec {

    @Override
    protected void encodeSubElements(ByteBuffer buffer, CompositeMap compositeMap, Map<String, Codec> subElementsCodecs) {
        Set<String> elementsToEncode = new TreeSet<>(compositeMap.keySet());
        for (Map.Entry<String, Codec> codecEntry : subElementsCodecs.entrySet()) {
            String index = codecEntry.getKey();
            Codec codec = codecEntry.getValue();
            if (BitmapCodec.class.equals(codec.getAttribute(CoreAttribute.CLASS))) {
                Bitmap bitmap = buildBitmap(compositeMap);
                compositeMap.put(index, bitmap);
            }
            Object messageElement = compositeMap.get(index);
            if (messageElement != null) {
                encodeSubElement(index, codec, buffer, messageElement);
                elementsToEncode.remove(index);
            }
        }
        if (elementsToEncode.size() > 0) {
            throw new RuntimeException("Unexpected sub elements " + elementsToEncode);
        }
    }

    private Bitmap buildBitmap(CompositeMap compositeMap) {
        Bitmap bitmap = new Bitmap();
        for (int i = 2; i < 129; i++) {
            if (compositeMap.containsKey(Integer.toString(i))) {
                bitmap.set(i);
            }
        }
        return bitmap;
    }

    @Override
    protected CompositeMap decodeSubElements(ByteBuffer buffer, Map<String, Codec> subElementsCodecs) {
        Bitmap bitmap = null;
        CompositeMap compositeMap = new CompositeMap();
        for (Map.Entry<String, Codec> codecEntry : subElementsCodecs.entrySet()) {
            String index = codecEntry.getKey();
            Codec codec = codecEntry.getValue();
            Object subElement = decodeSubElement(index, codec, buffer);
            compositeMap.put(index, subElement);
            if (BitmapCodec.class.equals(codec.getAttribute(CoreAttribute.CLASS))) {
                bitmap = (Bitmap) subElement;
                break;
            }
        }

        for (Integer bit : bitmap) {
            String index = bit.toString();
            Codec codec = subElementsCodecs.get(index);
            if (codec != null) {
                Object subElement = decodeSubElement(index, codec, buffer);
                compositeMap.put(index, subElement);
            } else {
                throw new CodecException("No codec defined", index);
            }
        }

        return compositeMap;
    }

}
