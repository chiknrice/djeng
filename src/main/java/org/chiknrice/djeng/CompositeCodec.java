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
import java.util.Map;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public abstract class CompositeCodec extends BaseCodec<CompositeMap> {

    protected final void encodeSubElement(String index, Codec codec, ByteBuffer buffer, MessageElement element) {
        try {
            codec.encode(buffer, element);
        } catch (CodecException ce) {
            if (isCompositeCodec(codec)) {
                ce.addParentIndex(index);
            }
            throw ce;
        } catch (Exception e) {
            throw new CodecException(e, index);
        }
    }

    protected final MessageElement decodeSubElement(String index, Codec codec, ByteBuffer buffer) {
        try {
            return codec.decode(buffer);
        } catch (CodecException ce) {
            if (isCompositeCodec(codec)) {
                ce.addParentIndex(index);
            }
            throw ce;
        } catch (Exception e) {
            throw new CodecException(e, index);
        }
    }

    @Override
    public final void encode(ByteBuffer buffer, MessageElement<CompositeMap> element) {
        Map<String, Codec> subElementsCodecs = getAttribute(CoreAttribute.SUB_ELEMENT_CODECS_MAP);
        encodeSubElements(buffer, element.getValue(), subElementsCodecs);
    }

    protected abstract void encodeSubElements(ByteBuffer buffer, CompositeMap compositeMap, Map<String, Codec> subElementsCodecs);

    @Override
    public final MessageElement<CompositeMap> decode(ByteBuffer buffer) {
        Map<String, Codec> subElementsCodecs = getAttribute(CoreAttribute.SUB_ELEMENT_CODECS_MAP);
        return new MessageElement<>(decodeSubElements(buffer, subElementsCodecs));
    }

    protected abstract CompositeMap decodeSubElements(ByteBuffer buffer, Map<String, Codec> subElementsCodecs);

    static boolean isCompositeCodec(Codec codec) {
        Codec elementCodec = codec;
        while (elementCodec instanceof CodecFilter) {
            elementCodec = ((CodecFilter) elementCodec).getChain();
        }
        return elementCodec instanceof CompositeCodec;
    }

}
