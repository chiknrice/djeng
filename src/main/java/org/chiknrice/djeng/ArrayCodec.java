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
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public abstract class ArrayCodec extends CodecFilter<CompositeMap> {

    @Override
    protected void encode(ByteBuffer buffer, MessageElement<CompositeMap> element, Codec chain) {
        CompositeMap compositeMap = element.getValue();
        // TODO finish this
    }

    @Override
    protected MessageElement<CompositeMap> decode(ByteBuffer buffer, Codec chain) {
        CompositeMap compositeMap = new CompositeMap();
        MessageElement<CompositeMap> element = new MessageElement<>(compositeMap);
        // TODO finish this
        return element;
    }
}
