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

/**
 * The {@code Codec<T>} interface defines the fundamental contract of a codec which is to encode a value {@code T} to a
 * {@code java.nio.ByteBuffer} and decode the bytes from the {@code ByteBuffer} to a value {@code T}.  The {@code
 * ByteBuffer} acts as a window to the backing byte array rather than creating and copying byte arrays when processing
 * each message element.
 * <p>
 * A codec is also capable of having attributes which can drive how the encoding/decoding are performed.  Since codecs
 * can be used in multiple parts of a message, the attributes are specific to a context in where the codec is used.  A
 * codec's documentation should mention the set of attributes supported and which are required.  The documentation
 * should also mention what the effects of the attributes and their values to the encoding/decoding process.
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public interface Codec<T> {

    /**
     * Encodes the value {@code T} to bytes and writes it to the {@code ByteBuffer} from its current position.  Encoding
     * should expect a non-{@code null} value.
     *
     * @param buffer  where the encoded bytes are written to
     * @param element the non-null value which would be encoded
     */
    void encode(ByteBuffer buffer, T element);

    /**
     * Decodes the bytes in the {@code java.nio.ByteBuffer} (or portion of it) from the buffer's current position to a
     * value {@code T}.
     *
     * @param buffer the source of the bytes to decode
     * @return the decoded value
     */
    T decode(ByteBuffer buffer);

    /**
     * Gets the codec's attribute.
     *
     * @param attribute the attribute's ID to get
     * @param <A>       the expected type of the attribute value
     * @return the attribute value, or {@code null} if the attribute wasn't set
     */
    <A> A getAttribute(Attribute attribute);

}
