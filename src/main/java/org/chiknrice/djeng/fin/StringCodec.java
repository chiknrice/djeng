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

import org.chiknrice.djeng.ElementCodec;

import java.nio.charset.StandardCharsets;

/**
 * TODO: describe usage and document what properties are required/optional to be set
 *
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class StringCodec extends ElementCodec<String> {

    @Override
    protected byte[] encodeValue(String value) {
        Integer length = getAttribute(FinancialAttribute.LENGTH);
        if (length != null) {
            Boolean leftJustifiedAttr = getAttribute(FinancialAttribute.LEFT_JUSTIFIED);
            boolean leftJustified = leftJustifiedAttr != null && leftJustifiedAttr;
            value = String.format("%" + (leftJustified ? "-" : "") + length + "s", value);
        }
        return value.getBytes(StandardCharsets.ISO_8859_1);
    }

    @Override
    protected String decodeValue(byte[] bytes) {
        String decoded = new String(bytes, StandardCharsets.ISO_8859_1);
        Boolean stripPadding = getAttribute(FinancialAttribute.STRIP_PADDING);
        if (stripPadding != null && stripPadding) {
            decoded = decoded.trim();
        }
        return decoded;
    }

}
