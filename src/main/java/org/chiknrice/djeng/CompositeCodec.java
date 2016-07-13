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
            // TODO: clean this up
//            System.out.println(String.format("%5s : %s", index, element.getValue()));
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
            MessageElement element = codec.decode(buffer);
            // TODO: clean this up
//            System.out.println(String.format("%5s : %s", index, element.getValue()));
            return element;
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
    public void encode(ByteBuffer buffer, MessageElement<CompositeMap> element) {
        int pos = buffer.arrayOffset() + buffer.position();
        Map<String, Codec<?>> subElementsCodecs = getAttribute(CoreAttributes.SUB_ELEMENT_CODECS_MAP);
        encodeSubElements(buffer, element.getValue(), subElementsCodecs);
        element.addSection(super.<String>getAttribute(CoreAttributes.INDEX), pos, buffer.arrayOffset() + buffer.position());
    }

    protected abstract void encodeSubElements(ByteBuffer buffer, CompositeMap compositeMap, Map<String, Codec<?>> subElementsCodecs);

    @Override
    public MessageElement<CompositeMap> decode(ByteBuffer buffer) {
        int pos = buffer.arrayOffset() + buffer.position();
        Map<String, Codec<?>> subElementsCodecs = getAttribute(CoreAttributes.SUB_ELEMENT_CODECS_MAP);
        MessageElement<CompositeMap> element = new MessageElement<>(decodeSubElements(buffer, subElementsCodecs));
        element.addSection(super.<String>getAttribute(CoreAttributes.INDEX), pos, buffer.arrayOffset() + buffer.position());
        return element;
    }

    protected abstract CompositeMap decodeSubElements(ByteBuffer buffer, Map<String, Codec<?>> subElementsCodecs);

    static boolean isCompositeCodec(Codec<?> codec) {
        Codec<?> elementCodec = codec;
        while (elementCodec instanceof CodecFilter) {
            elementCodec = ((CodecFilter) elementCodec).getChain();
        }
        return elementCodec instanceof CompositeCodec;
    }

}
