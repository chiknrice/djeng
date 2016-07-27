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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class StructDataCodec extends StringCodec {
    @Override
    protected void putDataBytes(ByteBuffer buffer, byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        String length = String.valueOf(bytes.length);
        sb.append(length.length());
        sb.append(length);
        buffer.put(sb.toString().getBytes(StandardCharsets.ISO_8859_1));
        buffer.put(bytes);
    }

    @Override
    protected byte[] getDataBytes(ByteBuffer buffer) {
        int lengthByteCount = Integer.parseInt(new String(new byte[]{buffer.get()}, StandardCharsets.ISO_8859_1));
        byte[] lengthBytes = new byte[lengthByteCount];
        buffer.get(lengthBytes);
        int dataBytesCount = Integer.parseInt(new String(lengthBytes, StandardCharsets.ISO_8859_1));
        byte[] dataBytes = new byte[dataBytesCount];
        buffer.get(dataBytes);
        return dataBytes;
    }

}
