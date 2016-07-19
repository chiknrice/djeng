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

import java.nio.ByteBuffer;

import static org.chiknrice.djeng.CodecContext.*;

/**
 * Encodes and decodes a {@link Message} to and from a {@code byte[]}
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class MessageCodec {

    private final MessageCodecConfig config;

    public MessageCodec(MessageCodecConfig config) {
        this.config = config;
    }

    /**
     * Encodes the {@code Message} to {@code byte[]} based on the rules defined by the config.
     *
     * @param message the message to be encoded.
     * @return the encoded bytes.
     */
    public byte[] encode(Message message) {
        ByteBuffer buffer = ByteBuffer.allocate(config.getEncodeBufferSize());
        try {
            message.rwLock.writeLock().lock();
            clear();
            setDebugEnabled(config.isDebugEnabled());
            config.getRootCodec().encode(buffer, message.getCompositeMap());
            byte[] encoded = new byte[buffer.position()];
            buffer.rewind();
            buffer.get(encoded);
            return encoded;
        } finally {
            message.rwLock.writeLock().unlock();
            dumpLogs();
        }
    }

    /**
     * Decodes the {@code byte[]} to a {@code Message} based on the rules defined by the config.
     *
     * @param messageBytes the bytes to decode.
     * @return the decoded Message.
     */
    public Message decode(byte[] messageBytes) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(messageBytes);
            clear();
            setDebugEnabled(config.isDebugEnabled());
            CompositeMap element = config.getRootCodec().decode(buffer);
            Message message = new Message(element);
            return message;
        } finally {
            dumpLogs();
        }
    }

}
