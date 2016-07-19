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

import static org.chiknrice.djeng.CodecContext.*;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public abstract class CompositeCodec extends BaseCodec<CompositeMap> {

    protected final void encodeSubElement(String index, Codec codec, ByteBuffer buffer, Object element) {
        try {
            pushIndex(index);
            codec.encode(buffer, element);
        } catch (CodecException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CodecException(e, getCurrentIndexPath());
        } finally {
            popIndex();
        }
    }

    protected final Object decodeSubElement(String index, Codec codec, ByteBuffer buffer) {
        try {
            pushIndex(index);
            return codec.decode(buffer);
        } catch (CodecException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CodecException(e, getCurrentIndexPath());
        } finally {
            popIndex();
        }
    }

    @Override
    public final void encode(ByteBuffer buffer, CompositeMap element) {
        Map<String, Codec> subElementsCodecs = getAttribute(CoreAttribute.SUB_ELEMENT_CODECS_MAP);
        encodeSubElements(buffer, element, subElementsCodecs);
    }

    protected abstract void encodeSubElements(ByteBuffer buffer, CompositeMap compositeMap, Map<String, Codec> subElementsCodecs);

    @Override
    public final CompositeMap decode(ByteBuffer buffer) {
        Map<String, Codec> subElementsCodecs = getAttribute(CoreAttribute.SUB_ELEMENT_CODECS_MAP);
        return decodeSubElements(buffer, subElementsCodecs);
    }

    protected abstract CompositeMap decodeSubElements(ByteBuffer buffer, Map<String, Codec> subElementsCodecs);

}
