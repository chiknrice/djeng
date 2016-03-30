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

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class StructDataCodec extends ByteArrayBasedCodec<Map<String, String>> {

    @Override
    protected byte[] encodeValue(Map<String, String> value) {
        Map<String, String> map = value;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> kvPair : map.entrySet()) {
            String length = String.valueOf(kvPair.getKey().length());
            sb.append(length.length());
            sb.append(length);
            sb.append(kvPair.getKey());
            length = String.valueOf(kvPair.getValue().length());
            sb.append(length.length());
            sb.append(length);
            sb.append(kvPair.getValue());
        }
        return sb.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    @Override
    protected Map<String, String> decodeValue(byte[] rawValue) {
        String text = new String(rawValue, StandardCharsets.ISO_8859_1);
        int start = 0;
        int end = 1;
        Map<String, String> map = new LinkedHashMap<>();
        int segment = 1;
        String key = null;
        String tmp;
        while (start < text.length()) {
            tmp = text.substring(start, end);
            start = end;
            if (segment != 3 && segment != 6) {
                end += Integer.parseInt(tmp);
            } else if (segment == 3) {
                key = tmp;
                end += 1;
            } else {
                map.put(key, tmp);
                segment = 0;
                end += 1;
            }
            segment++;
        }
        return map;
    }

}
