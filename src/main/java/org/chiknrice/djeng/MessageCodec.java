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

import static org.chiknrice.djeng.MessageElement.*;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class MessageCodec {

    final CompositeCodec compositeCodec;

    MessageCodec(CompositeCodec compositeCodec) {
        this.compositeCodec = compositeCodec;
    }

    /**
     * Encodes the Message to bytes based on the rules defined by the config.
     *
     * @param message the message to be encoded.
     * @return the encoded bytes.
     */
    public byte[] encode(Message message) {
        message.clearMarkers();
        ByteBuffer buffer = ByteBuffer.allocate(0x7FFF);
        compositeCodec.encode(buffer, new MessageElement<>(message.getCompositeMap()));
        byte[] encoded = new byte[buffer.position()];
        buffer.rewind();
        buffer.get(encoded);
        message.messageBuffer = ByteBuffer.wrap(encoded);
        logMessage("ENCODE", message);
        return encoded;
    }

    /**
     * Decodes the isoBytes based on the rules defined by the config.
     *
     * @param messageBytes the bytes to decode.
     * @return the decoded Message.
     */
    public Message decode(byte[] messageBytes) {
        ByteBuffer buffer = ByteBuffer.wrap(messageBytes);
        MessageElement<CompositeMap> element = compositeCodec.decode(buffer);
        Message message = new Message(element.getValue());
        message.messageBuffer = buffer;
        logMessage("DECODE", message);
        return message;
    }

    private void logMessage(String operation, Message message) {
        // TODO: beautify this log
        // TODO: implement masking!
        //sortedMap.putAll(message.getElementsInternal());
        Map<Section, MessageElement<?>> elementMap = message.getElementsInternal();
        System.out.println(operation);
        for (Map.Entry<Section, MessageElement<?>> entry : elementMap.entrySet()) {
            System.out.println(String.format("%10s : %s", entry.getKey().index, entry.getValue()));
        }

        //for (Map.Entry<String, Object> elementEntry : sortedMap.entrySet()) {
            //System.out.println(String.format("%10s : %s", elementEntry.getKey(), elementEntry.getValue()));
            //System.out.println(String.format("%10s : %s", "%h", rawElementsMap.get(elementEntry.getKey())));
        //}
    }

}
