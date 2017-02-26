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

/**
 * The {@code ByteArrayCodec} class encodes/decodes byte array elements.
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class ByteArrayCodec extends ElementCodec<byte[]> {

    // TODO: implement padding

    @Override
    protected byte[] encodeValue(byte[] value) {
        return value;
    }

    @Override
    protected byte[] decodeValue(byte[] bytes) {
        return bytes;
    }
}
