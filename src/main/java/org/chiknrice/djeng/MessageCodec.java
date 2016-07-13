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
 */

package org.chiknrice.djeng;

/**
 * Encodes and decodes a {@link Message} to and from a byte[]
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public interface MessageCodec {

    /**
     * Encodes the Message to bytes based on the rules defined by the config.
     *
     * @param message the message to be encoded.
     * @return the encoded bytes.
     */
    byte[] encode(Message message);

    /**
     * Decodes the isoBytes based on the rules defined by the config.
     *
     * @param messageBytes the bytes to decode.
     * @return the decoded Message.
     */
    Message decode(byte[] messageBytes);
}
