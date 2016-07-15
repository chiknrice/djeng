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
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A MessageElement is a portion of a message...
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class MessageElement<T> {

    private final T value;
    private final TreeSet<Section> sections;

    public MessageElement(T value) {
        if (value == null) throw new IllegalArgumentException("Null value is not allowed");
        this.value = value;
        this.sections = new TreeSet<>();
    }

    static class Section implements Comparable<Section> {
        final int pos;
        final int limit;
        final Object value;
        String path;

        private Section(int pos, int limit, Object value) {
            this.pos = pos;
            this.limit = limit;
            this.value = value;
        }

        @Override
        public int compareTo(Section o) {
            if (this.pos < o.pos) {
                if (this.limit > o.pos) {
                    throw new RuntimeException("Overlapping sections");
                }
                return -1;
            } else if (o.pos < this.pos) {
                if (o.limit > this.pos) {
                    throw new RuntimeException("Overlapping sections");
                }
                return 1;
            } else {
                if (this.limit != o.limit) {
                    throw new RuntimeException("Overlapping sections");
                }
                return 0;
            }
        }

    }

    SortedSet<Section> getSections() {
        return sections;
    }

    void clearSections() {
        sections.clear();
        if (value instanceof CompositeMap) {
            for (MessageElement<?> subElement : ((CompositeMap) value).values()) {
                subElement.clearSections();
            }
        }
    }

    public void addSectionsFrom(MessageElement<?> element) {
        sections.addAll(element.getSections());
    }

    public void addSection(int pos, int limit, Object value) {
        sections.add(new Section(pos, limit, value));
    }

    public T getValue() {
        return this.value;
    }

    @Override
    // TODO: determine if hashcode is needed to be implemented, where does message element identities matter?
    public int hashCode() {
        return value == null ? 0 : value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof MessageElement) {
            MessageElement element = (MessageElement) obj;
            if (this.value == null) {
                return element.value == null;
            } else if (value instanceof byte[]) {
                return Arrays.equals((byte[]) this.value, (byte[]) element.value);
            } else {
                return this.value.equals(element.value);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        if (value instanceof byte[]) {
            return "0x".concat(ByteUtil.encodeHex((byte[]) value));
        } else {
            return value.toString();
        }
    }
}
