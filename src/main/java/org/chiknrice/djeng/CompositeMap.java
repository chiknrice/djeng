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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The {@code CompositeMap} is a tweak to the {@code java.util.HashMap} which
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class CompositeMap extends HashMap<String, Object> {

    /**
     * Restricted implementation of {@code HashMap#put} to non-null keys and values.
     *
     * @param key   TODO
     * @param value TODO
     * @return TODO
     */
    @Override
    public Object put(String key, Object value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Composite map cannot have null keys or values");
        }
        return super.put(key, value);
    }

    /**
     * Improved implementation to {@code HashMap#equals} which considers {@code byte[]} values.
     *
     * @param o TODO
     * @return TODO
     */
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Map))
            return false;
        Map<?, ?> m = (Map<?, ?>) o;
        if (m.size() != size())
            return false;

        try {
            Iterator<Map.Entry<String, Object>> i = entrySet().iterator();
            while (i.hasNext()) {
                Map.Entry<String, Object> e = i.next();
                String key = e.getKey();
                Object value = e.getValue();
                if (value == null) {
                    if (!(m.get(key) == null && m.containsKey(key)))
                        return false;
                } else {
                    if (value instanceof byte[]) {
                        if (!Arrays.equals((byte[]) value, (byte[]) m.get(key))) {
                            return false;
                        }
                    } else if (!value.equals(m.get(key))) {
                        return false;
                    }
                }
            }
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }

        return true;
    }

    /**
     * Improved implementation to {@code HashMap#hashcode} which considers {@code byte[]} values.
     *
     * @return TODO
     */
    @Override
    public int hashCode() {
        int h = 0;
        Iterator<Map.Entry<String, Object>> i = entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, Object> next = i.next();
            if (next.getValue() instanceof byte[]) {
                h += next.getKey().hashCode() ^ Arrays.hashCode((byte[]) next.getValue());
            } else {
                h += next.hashCode();
            }
        }

        return h;
    }

}
