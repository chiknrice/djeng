package org.chiknrice.djeng;

import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class SectionRecordingFilter<T> extends CodecFilter<T> {

    @Override
    protected void encode(ByteBuffer buffer, MessageElement<T> element, Codec chain) {
        int pos = buffer.arrayOffset() + buffer.position();
        chain.encode(buffer, element);
        element.addSection(pos, buffer.arrayOffset() + buffer.position(), element.getValue());
    }

    @Override
    protected MessageElement<T> decode(ByteBuffer buffer, Codec<?> chain) {
        int pos = buffer.arrayOffset() + buffer.position();
        MessageElement element = chain.decode(buffer);
        element.addSection(pos, buffer.arrayOffset() + buffer.position(), element.getValue());
        return element;
    }

}
