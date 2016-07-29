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
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

/**
 * The {@code Codec<T>} interface defines the fundamental contract of a codec which is to encode a value {@code T} to a
 * {@code java.nio.ByteBuffer} and decode the bytes from the {@code ByteBuffer} to a value {@code T}.  The {@code
 * ByteBuffer} acts as a window to the backing byte array rather than creating and copying byte arrays when processing
 * each message element.
 * <p/>
 * A codec is also capable of having attributes which can drive how the encoding/decoding are performed.  Since codecs
 * can be used in multiple parts of a message, the attributes are specific to a context in where the codec is used.  A
 * codec's documentation should mention the set of attributes supported and which are required.  The documentation
 * should also mention what the effects of the attributes and their values to the encoding/decoding process.
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public abstract class Codec<T> {

    /**
     * Encodes the value {@code T} to bytes and writes it to the {@code ByteBuffer} from its current position.  Encoding
     * should expect a non-{@code null} value.
     *
     * @param buffer  where the encoded bytes are written to
     * @param element the non-null value which would be encoded
     */
    public abstract void encode(ByteBuffer buffer, T element);

    /**
     * Decodes the bytes in the {@code java.nio.ByteBuffer} (or portion of it) from the buffer's current position to a
     * value {@code T}.
     *
     * @param buffer the source of the bytes to decode
     * @return the decoded value
     */
    public abstract T decode(ByteBuffer buffer);

    Map<Attribute, Object> attributes;

    /**
     * Gets the codec's attribute.
     *
     * @param attribute the attribute's ID to get
     * @param <A>       the expected type of the attribute value
     * @return the attribute value, or {@code null} if the attribute wasn't set
     */
    public <A> A getAttribute(Attribute attribute) {
        //noinspection unchecked
        return (A) attributes.get(attribute);
    }

    private static final ThreadLocal<Boolean> RECORDING_SECTION = new ThreadLocal<>();
    private static final ThreadLocal<Stack<String>> INDEX_STACK = new ThreadLocal<>();
    private static final ThreadLocal<SortedSet<Section>> SECTIONS = new ThreadLocal<>();

    protected void pushIndex(String index) {
        INDEX_STACK.get().push(index);
    }

    protected void popIndex() {
        Stack<String> indexStack = INDEX_STACK.get();
        if (indexStack == null) {
            throw new IllegalStateException("No existing " + INDEX_STACK);
        }
        indexStack.pop();
    }

    protected void recordSection(int pos, int len, Object value, ByteBuffer buffer) {
        if (RECORDING_SECTION.get()) {
            SortedSet<Section> sections = SECTIONS.get();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            sections.add(new Section(pos, len, getCurrentIndexPath(), value, ByteUtil.encodeHex(bytes)));
        }
    }

    protected String getCurrentIndexPath() {
        Stack<String> indexStack = INDEX_STACK.get();
        StringBuilder indexPath = new StringBuilder();
        for (String index : indexStack) {
            if (indexPath.length() > 0) {
                indexPath.append(".");
            }
            indexPath.append(index);
        }
        return indexPath.toString();
    }

    protected void suspendRecordingSections() {
        RECORDING_SECTION.set(Boolean.FALSE);
    }

    protected void resumeRecordingSections() {
        RECORDING_SECTION.set(Boolean.TRUE);
    }

    void dumpLogs(boolean printLogs) {
        // TODO is this what we want to do with the sections?
        StringBuilder sb = new StringBuilder();
        int expectedPos = 0;
        for (Section section : SECTIONS.get()) {
            if (section.pos != expectedPos) {
                //throw new RuntimeException("Expecting pos " + expectedPos + ", got " + section.pos);
                System.err.println("Expecting pos " + expectedPos + ", got " + section.pos);
            }
            expectedPos += section.len;
            if (printLogs) {
                System.err.println(section);
            }
            sb.append(section.hex);
        }
        if (printLogs) {
            System.err.println(sb.toString());
        }
    }

    void startRecordingSections() {
        RECORDING_SECTION.set(Boolean.TRUE);
        INDEX_STACK.set(new Stack<String>());
        SECTIONS.set(new TreeSet<Section>());
    }

    void stopRecordingSections() {
        RECORDING_SECTION.remove();
        INDEX_STACK.remove();
        SECTIONS.remove();
    }

    private static class Section implements Comparable<Section> {

        final int pos;
        final int len;
        final String indexPath;
        final Object value;
        final String hex;

        Section(int pos, int len, String indexPath, Object value, String hex) {
            this.pos = pos;
            this.len = len;
            this.indexPath = indexPath;
            this.value = value;
            this.hex = hex;
        }

        @Override
        public int compareTo(Section o) {
            return pos < o.pos ? -1 : pos > o.pos ? 1 : 0;
        }

        @Override
        public String toString() {
            int leftPad = 5 - (indexPath.contains(".") ? indexPath.indexOf(".") : indexPath.length());
            int rightPad = 20 - leftPad - indexPath.length();
            return String.format("%5d[%5d]|%" + leftPad + "s%s%" + rightPad + "s%40s | 0x%-40s", pos, len, "", indexPath, "", value, hex);
        }
    }

}
