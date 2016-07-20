package org.chiknrice.djeng.fin;

import org.chiknrice.djeng.ByteUtil;

import static org.chiknrice.djeng.fin.FinancialAttribute.PACKED;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class Track2Codec extends StringCodec implements LengthPrefixDelegate {

    @Override
    protected byte[] encodeValue(String value) {
        Boolean packed = getAttribute(PACKED);
        if (packed) {
            value = value.replace('=', 'D');
            return ByteUtil.decodeHex(value);
        } else {
            return super.encodeValue(value);
        }
    }

    @Override
    protected String decodeValue(byte[] bytes) {
        Boolean packed = getAttribute(PACKED);
        if (packed) {
            String packedTrack2 = ByteUtil.encodeHex(bytes);
            return packedTrack2.replace('D', '=');
        } else {
            return super.decodeValue(bytes);
        }
    }

    @Override
    public int determineLengthPrefixValue(Object value) {
        return value.toString().length();
    }

    @Override
    public int determineDataBytesCount(int lengthPrefix) {
        Boolean packed = getAttribute(PACKED);
        if (packed) {
            return lengthPrefix / 2 + lengthPrefix % 2;
        } else {
            return lengthPrefix;
        }
    }

}
