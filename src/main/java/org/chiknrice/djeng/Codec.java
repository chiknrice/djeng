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
 * The {@code Codec<T>} interface defines the fundamental contract of a codec which is to encode a value of a {@link
 * MessageElement} to a {@code java.nio.ByteBuffer} and decode the bytes from the {@code ByteBuffer} to a value
 * encapsulated in a {@code MessageElement}. The actual value {@code T} is encapsulated in a {@code MessageElement} together with information on to
 * provide a context when a value is being encoded/decoded.  The {@code ByteBuffer} on the other hand was chosen to for
 * performance as it would limit the use of byte arrays.  The buffer acts as a window to the backing byte array rather
 * than creating and copying byte arrays when processing each message element.
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
     * Encodes the {@code MessageElement} value {@code T} to bytes and writes it to the {@code ByteBuffer} from its
     * current position.  Encoding should expect the value of the element non-{@code null}.
     *
     * @param buffer  where the encoded bytes are written to
     * @param element contains the non-null value which would be encoded
     */
    void encode(ByteBuffer buffer, MessageElement<T> element);

    /**
     * Decodes the bytes in the {@code java.nio.ByteBuffer} (or portion of it) from the buffer's current position to a
     * value {@code T} and creates a new {@code MessageElement} containing that value.  The codec should not produce a
     * {@code  MessageElement} containing a {@code null} value.
     *
     * @param buffer the source of the bytes to decode
     * @return a new {@code MessageElement} containing the decoded value
     */
    MessageElement<T> decode(ByteBuffer buffer);

    /**
     * Gets the codec's attribute.
     *
     * @param attribute the attribute's ID to get
     * @param <A>       the expected type of the attribute value
     * @return the attribute value, or {@code null} if the attribute wasn't set
     */
    <A> A getAttribute(Attribute attribute);

    /**
     * Sets the codec's attribute
     *
     * @param attribute the attribute's ID to set
     * @param value     the value to be set
     */
    void setAttribute(Attribute attribute, Object value);

}
