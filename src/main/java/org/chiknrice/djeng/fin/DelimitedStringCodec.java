package org.chiknrice.djeng.fin;

import java.nio.ByteBuffer;

/**
 * The {@code DelimitedStringCodec} decodes stings that has a length up a particular character delimiter.  The delimiter
 * can be supplied via {@code delimiter} attribute which should be in the form of a hex string.  Currently, the
 * delimiter is expected to be a single byte character.
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class DelimitedStringCodec extends StringCodec {

    private Byte delimiter;

    private byte getDelimiter() {
        if (delimiter == null) {
            String delimiterHex = getAttribute(FinancialAttribute.DELIMITER);
            delimiter = new Byte((byte) Integer.parseInt(delimiterHex, 16));
        }
        return delimiter.byteValue();
    }

    /**
     * TODO
     *
     * @param buffer TODO
     * @return TODO
     */
    @Override
    protected byte[] getDataBytes(ByteBuffer buffer) {
        int limit = -1;
        buffer.mark();
        while (buffer.hasRemaining()) {
            byte currentByte = buffer.get();
            if (currentByte == getDelimiter()) {
                limit = buffer.position();
                break;
            }
        }
        // The last delimiter is optional
        if (!buffer.hasRemaining()) {
            limit = buffer.position();
        }
        buffer.reset();
        byte[] segment = new byte[limit - buffer.position()];
        buffer.get(segment);
        return segment;
    }

    /**
     * TODO
     *
     * @param bytes TODO
     * @return TODO
     */
    @Override
    protected String decodeValue(byte[] bytes) {
        String value = super.decodeValue(bytes);
        if (bytes.length > 0 && bytes[bytes.length - 1] == getDelimiter()) {
            return value.substring(0, value.length() - 1);
        } else {
            return value;
        }
    }

    /**
     * TODO
     *
     * @param buffer the ByteBuffer to which the bytes would be encoded
     * @param bytes  TODO
     */
    @Override
    protected void putDataBytes(ByteBuffer buffer, byte[] bytes) {
        super.putDataBytes(buffer, bytes);
        buffer.put(getDelimiter());
    }

}
