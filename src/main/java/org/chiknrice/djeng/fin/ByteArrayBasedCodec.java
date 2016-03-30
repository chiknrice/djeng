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

import org.chiknrice.djeng.ElementCodec;

import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
abstract class ByteArrayBasedCodec<T> extends ElementCodec<T, byte[]> {

    @Override
    protected void encodeRawValue(ByteBuffer buffer, byte[] value) {
        buffer.put(value);
    }

    @Override
    protected byte[] decodeRawValue(ByteBuffer buffer) {
        Integer length = getAttribute(FinancialAttributes.LENGTH);
        byte[] bytes;
        if (length != null) {
            bytes = new byte[length];
        } else {
            bytes = new byte[buffer.remaining()];
        }
        buffer.get(bytes);
        return bytes;
    }

}
