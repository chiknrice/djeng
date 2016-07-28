package org.chiknrice.djeng.fin;

import org.chiknrice.djeng.ByteUtil;
import org.chiknrice.djeng.Codec;
import org.chiknrice.djeng.CompositeCodec;
import org.chiknrice.djeng.CompositeMap;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.chiknrice.djeng.fin.FinancialAttribute.LENGTH;

/**
 * The {@code HexCompositeCodec} class expects the data in bytes and coverts it to hex prior to delegating to sub
 * element codecs.  TODO better wording? or better approach??? this is for DE90 only (for now)
 * <p/>
 * TODO: consider if this can probably be done as a CodecFilter to accommodate Track 2
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class HexCompositeCodec extends CompositeCodec {

    private Integer length = null;

    @Override
    protected void encodeSubElements(ByteBuffer buffer, CompositeMap compositeMap, Map<String, Codec> subElementsCodecs) {
        ByteBuffer tempBuffer = ByteBuffer.allocate(getLength(subElementsCodecs) * 2);
        try {
            suspendRecordingSections();
            super.encodeSubElements(tempBuffer, compositeMap, subElementsCodecs);
        } finally {
            resumeRecordingSections();
        }
        byte[] hexBytes = new byte[tempBuffer.position()];
        tempBuffer.flip();
        tempBuffer.get(hexBytes);
        String hex = new String(hexBytes, StandardCharsets.ISO_8859_1);
        int pos = buffer.arrayOffset() + buffer.position();
        byte[] bytes = ByteUtil.decodeHex(hex);
        buffer.put(bytes);
        recordSection(pos, bytes.length, "<composite>", ByteUtil.recallToBuffer(buffer, bytes.length));
    }

    @Override
    protected CompositeMap decodeSubElements(ByteBuffer buffer, Map<String, Codec> subElementsCodecs) {
        int pos = buffer.arrayOffset() + buffer.position();
        byte[] bytes = new byte[getLength(subElementsCodecs)];
        buffer.get(bytes);
        String hex = ByteUtil.encodeHex(bytes);
        ByteBuffer tempBuffer = ByteBuffer.wrap(hex.getBytes(StandardCharsets.ISO_8859_1));
        try {
            suspendRecordingSections();
            return super.decodeSubElements(tempBuffer, subElementsCodecs);
        } finally {
            resumeRecordingSections();
            recordSection(pos, bytes.length, "<composite>", ByteUtil.recallToBuffer(buffer, bytes.length));
        }
    }

    private Integer getLength(Map<String, Codec> subElementsCodecs) {
        if (length == null) {
            Integer tempLength = 0;
            for (Codec<?> codec : subElementsCodecs.values()) {
                Integer lengthAttribute = codec.getAttribute(LENGTH);
                if (lengthAttribute == null) {
                    String pattern = codec.getAttribute(LENGTH);
                    if (pattern != null) {
                        lengthAttribute = pattern.length();
                    }
                }
                if (lengthAttribute == null) {
                    throw new RuntimeException("Sub-elements are required to be fixed");
                }
                tempLength += lengthAttribute;
            }
            length = tempLength / 2 + tempLength % 2;
        }
        return length;
    }

}
