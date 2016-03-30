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

import org.chiknrice.djeng.ByteUtil;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class BitmapCodec extends ByteArrayBasedCodec<Bitmap> {

    @Override
    protected byte[] encodeValue(Bitmap bitmap) {
        int offset = 0;
        byte[] bytes = new byte[bitmap.encoding.primaryBitmapLength];
        // TODO: Is the bitmap up to 32 bytes (for HEX) only? how about data set bitmap?
        ByteBuffer buffer = ByteBuffer.allocate(32);
        for (int i = 1; i < 129; i++) {
            if (!bitmap.isSet(i)) {
                continue;
            } else {
                int byteIndex = Bitmap.byteIndex(i);
                while (byteIndex >= (bytes.length + offset)) {
                    if ((bytes[0] & 0x80) > 0) {
                        throw new RuntimeException("Extension bit should not be set");
                    }
                    bytes[0] |= 0x80;
                    buffer.put(Bitmap.Encoding.HEX.equals(bitmap.encoding) ? ByteUtil.encodeHex(bytes).getBytes(StandardCharsets.ISO_8859_1) : bytes);
                    offset += bytes.length;
                    bytes = new byte[bitmap.encoding.secondaryBitmapLength];
                }
                bytes[byteIndex - offset] |= Bitmap.mask(i);
            }
        }
        if ((bytes[bytes.length - 1] & 0x80) > 0) {
            throw new RuntimeException("Extension bit should not be set");
        }
        buffer.put(Bitmap.Encoding.HEX.equals(bitmap.encoding) ? ByteUtil.encodeHex(bytes).getBytes(StandardCharsets.ISO_8859_1) : bytes);
        bytes = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(bytes);
        return bytes;
    }

    @Override
    protected Bitmap decodeValue(byte[] rawValue) {
        Bitmap.Encoding encoding = getAttribute(FinancialAttributes.BITMAP_ENCODING);
        return new Bitmap(rawValue, encoding);
    }

    @Override
    protected byte[] decodeRawValue(ByteBuffer buffer) {
        Bitmap.Encoding encoding = getAttribute(FinancialAttributes.BITMAP_ENCODING);
        byte[] bytes;
        switch (encoding) {
            case BINARY:
                buffer.mark();
                if ((buffer.get() & 0x80) == 0) {
                    bytes = new byte[8];
                } else {
                    bytes = new byte[16];
                }
                buffer.reset();
                buffer.get(bytes);
                break;
            case HEX:
                buffer.mark();
                if ((ByteUtil.hexValue((char) buffer.get()) & 0x8) == 0) {
                    bytes = new byte[16];
                } else {
                    bytes = new byte[32];
                }
                buffer.reset();
                buffer.get(bytes);
                bytes = ByteUtil.decodeHex(new String(bytes, StandardCharsets.ISO_8859_1));
                break;
            case DATA_SET:
                buffer.mark();
                int total = 0;
                bytes = new byte[2];
                boolean hasNext;
                do {
                    buffer.get(bytes);
                    total += bytes.length;
                    hasNext = (bytes[0] & 0x80) > 0;
                    bytes = new byte[1];
                } while (hasNext);
                buffer.reset();
                bytes = new byte[total];
                buffer.get(bytes);
                break;
            default:
                throw new RuntimeException("Unsupported bitmap encoding " + encoding);
        }
        return bytes;
    }

    public static void main(String[] args) {
        System.out.println((((byte) 0x80) & 0xFF) >>> 7);
        System.out.println((ByteUtil.decodeHex("80")[0] & 0xFF) >>> 7);
        System.out.println(Integer.toBinaryString((byte) 0x80));
    }

}
