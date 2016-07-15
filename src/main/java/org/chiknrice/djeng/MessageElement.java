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
import java.util.Set;
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
        String index;
        int pos;
        int limit;

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
                if (this.limit != o.limit && !this.index.equals(o.index)) {
                    throw new RuntimeException("Overlapping sections");
                }
                return 0;
            }
        }
    }

    public Set<Section> getSections() {
        return sections;
    }

    public void addSection(String index, int pos, int limit) {
        Section section = new Section();
        section.index = index;
        section.pos = pos;
        section.limit = limit;
        sections.add(section);
    }

    public void copySections(MessageElement<?> element) {
        for (Section section : element.sections) {
            addSection(section.index, section.pos, section.limit);
        }
    }

    public int getPos() {
        return sections.first().pos;
    }

    public int getLimit() {
        return sections.last().limit;
    }

    public T getValue() {
        return this.value;
    }

    void clearMarkers() {
        sections.clear();
        if (value instanceof CompositeMap) {
            for (MessageElement<?> subElement : ((CompositeMap) value).values()) {
                subElement.clearMarkers();
            }
        }
    }

    @Override
    // TODO: determine if hashcode is needed to be implemented, where does message element identities matter?
    public int hashCode() {
        return value == null ? 0 : value.hashCode();
    }

    @Override
    public String toString() {
        if (value instanceof byte[]) {
            return "0x".concat(ByteUtil.encodeHex((byte[]) value));
        } else {
            return value.toString();
        }
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
}
