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
package org.chiknrice.djeng.fin;

import org.chiknrice.djeng.ByteUtil;
import org.chiknrice.djeng.ElementCodec;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class DateCodec extends ElementCodec<Date> {

    @Override
    protected byte[] encodeValue(Date value) {
        String pattern = getAttribute(FinancialAttributes.PATTERN);
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setLenient(false);
        String dateString = format.format(value);
        Encoding encoding = getAttribute(FinancialAttributes.DATE_ENCODING);
        byte[] bytes;
        switch (encoding) {
            case CHAR:
                bytes = dateString.getBytes(StandardCharsets.ISO_8859_1);
                break;
            case BCD:
                bytes = ByteUtil.encodeBcd(dateString);
                break;
            default:
                throw new RuntimeException("Unsupported date encoding " + encoding);
        }
        return bytes;
    }

    @Override
    protected byte[] decodeRawValue(ByteBuffer buffer) {
        String pattern = getAttribute(FinancialAttributes.PATTERN);
        Encoding encoding = getAttribute(FinancialAttributes.DATE_ENCODING);
        int length = pattern.length();
        switch (encoding) {
            case CHAR:
                // alread set to pattern length
                break;
            case BCD:
                length = length / 2 + length % 2;
                break;
            default:
                throw new RuntimeException("Unsupported date encoding " + encoding);

        }
        byte[] bytes = new byte[length];
        buffer.get(bytes);
        return bytes;
    }

    @Override
    protected Date decodeValue(byte[] rawValue) {
        String pattern = getAttribute(FinancialAttributes.PATTERN);
        Encoding encoding = getAttribute(FinancialAttributes.DATE_ENCODING);
        String dateString;
        switch (encoding) {
            case CHAR:
                dateString = new String(rawValue, StandardCharsets.ISO_8859_1);
                break;
            case BCD:
                dateString = ByteUtil.decodeBcd(rawValue);
                break;
            default:
                throw new RuntimeException("Unsupported date encoding " + encoding);

        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setLenient(false);
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
